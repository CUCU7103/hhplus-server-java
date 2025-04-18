package kr.hhplus.be.server.application.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.application.reservation.ReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationInfo;
import kr.hhplus.be.server.application.reservation.ReservationService;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.presentation.reservation.ReservationRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ReservationServiceUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ReservationService reservationService;

	@Test
	void 예약이_불가능한_좌석이라면_예외처리() {
		//arrange
		long concertId = 1L;
		long concertScheduleId = 1L;
		long seatId = 1L;
		long userId = 1L;
		LocalDate concertDate = LocalDate.of(2025, 6, 1);
		// 실제 Concert 엔티티 준비

		ConcertSchedule concertSchedule = mock(ConcertSchedule.class);

		// DTO (Request) 객체 준비
		ReservationRequest request = new ReservationRequest(concertScheduleId, concertDate);

		// stub
		// repository가 Optional.empty()를 반환하도록 설정
		given(concertRepository.getConcertSchedule(request.toCommand().concertScheduleId(),
			request.toCommand().concertScheduleDate())).willReturn(Optional.of(concertSchedule));
		given(concertRepository.getConcertSeatWhere(seatId, request.toCommand().concertScheduleId(),
			request.toCommand()
				.concertScheduleDate(), ConcertSeatStatus.AVAILABLE)).willReturn(Optional.empty());

		// act & assert
		assertThatThrownBy(() -> reservationService.reservationSeat(concertId, userId, request.toCommand()))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_RESERVATION_CONCERT_SEAT.getMessage());

		verify(concertRepository, times(1)).getConcertSeatWhere(
			seatId,
			request.toCommand().concertScheduleId(),
			request.toCommand().concertScheduleDate(),
			ConcertSeatStatus.AVAILABLE
		);

	}

	@Test
	void 예약_가능한_좌석이라면_예약에_성공한다() {
		// Arrange
		long concertScheduleId = 1L;
		long seatId = 1L;
		long userId = 1L;
		LocalDate concertScheduleDate = LocalDate.of(2025, 6, 1);
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		// 모의 객체 준비
		ConcertSchedule concertSchedule = org.mockito.Mockito.mock(ConcertSchedule.class);
		User user = org.mockito.Mockito.mock(User.class);
		given(user.getId()).willReturn(userId);

		// 좌석 객체 생성 및 상태 설정 (AVAILABLE 상태임을 보장)
		ConcertSeat seat = ConcertSeat.of(concertSchedule, ConcertSeatStatus.AVAILABLE, 11, "A", seatId, price);

		// Command DTO 생성
		ReservationCommand command = new ReservationCommand(concertScheduleId, concertScheduleDate);

		// Repository 스터빙
		given(concertRepository.getConcertSchedule(concertScheduleId, concertScheduleDate))
			.willReturn(Optional.of(concertSchedule));
		given(concertRepository.getConcertSeatWhere(seatId, concertScheduleId, concertScheduleDate,
			ConcertSeatStatus.AVAILABLE))
			.willReturn(Optional.of(seat));
		given(userRepository.findById(userId))
			.willReturn(Optional.of(user));
		// 스터빙을 통해, 실제 생성된 예약 객체를 그대로 반환하도록 함
		// 저장 로직이 호출될 때, 실제로 서비스 메서드가 생성한 예약 객체를 그대로 반환하도록 합니다.
		// 이렇게 하면 좌석의 changeStatus 메서드는 단 한 번 호출됩니다.
		given(reservationRepository.save(any(Reservation.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// Act
		ReservationInfo result = reservationService.reservationSeat(seatId, userId, command);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.price()).isEqualTo(price);
		// 예약 후 도메인에서는 HELD 상태를 할당하므로, 반환된 결과도 HELD 상태여야 합니다.
		assertThat(result.status()).isEqualTo(ReservationStatus.HELD);
		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.seatId()).isEqualTo(seatId);
	}

	@Test
	void 예약_대기시간_초과시_held_상태인_예약은_취소된다() {
		// arrange
		// HELD 상태인 예약 객체를 목 처리합니다.
		Reservation reservation1 = mock(Reservation.class);
		Reservation reservation2 = mock(Reservation.class);
		List<Reservation> heldReservations = Arrays.asList(reservation1, reservation2);

		// repository 스터빙: HELD 상태의 예약 리스트 반환
		given(reservationRepository.getConcertReservationStatus(ReservationStatus.HELD))
			.willReturn(heldReservations);

		// act
		reservationService.concertReservationCancel();

		// assert
		// 각 예약에 대해 cancel() 메서드가 호출되었음을 검증합니다.
		verify(reservation1, times(1)).cancelDueToTimeout(any(LocalDateTime.class));
		verify(reservation2, times(1)).cancelDueToTimeout(any(LocalDateTime.class));
	}
}
