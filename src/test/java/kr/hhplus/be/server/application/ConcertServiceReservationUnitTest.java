package kr.hhplus.be.server.application;

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

import kr.hhplus.be.server.domain.MoneyVO;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertReservation;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.command.ConcertReservationCommand;
import kr.hhplus.be.server.domain.concert.info.ConcertReservationInfo;
import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.concert.request.ConcertReservationRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ConcertServiceReservationUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ConcertService concertService;

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
		ConcertReservationRequest request = new ConcertReservationRequest(concertScheduleId, concertDate, userId);

		// stub
		// repository가 Optional.empty()를 반환하도록 설정
		given(concertRepository.getConcertSchedule(request.toCommand().concertScheduleId(),
			request.toCommand().concertScheduleDate())).willReturn(Optional.of(concertSchedule));
		given(concertRepository.getConcertSeatWhere(seatId, request.toCommand().concertScheduleId(), request.toCommand()
			.concertScheduleDate(), ConcertSeatStatus.AVAILABLE)).willReturn(Optional.empty());

		// act & assert
		assertThatThrownBy(() -> concertService.reservationSeat(concertId, request.toCommand()))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_RESERVATION_CONCERT_SEAT.getMessage());

		verify(concertRepository, times(1)).getConcertSeatWhere(
			seatId,
			request.toCommand().concertScheduleId(),
			request.toCommand().concertScheduleDate(),
			ConcertSeatStatus.AVAILABLE
		);

		verify(concertRepository, times(0)).save(any(ConcertReservation.class));
	}

	@Test
	void 예약_가능한_좌석이라면_예약에_성공한다() {
		// Arrange
		Long concertScheduleId = 1L;
		Long seatId = 1L;
		Long userId = 1L;
		LocalDate concertScheduleDate = LocalDate.of(2025, 6, 1);
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));

		// 모의 객체 준비
		ConcertSchedule concertSchedule = org.mockito.Mockito.mock(ConcertSchedule.class);
		User user = org.mockito.Mockito.mock(User.class);
		when(user.getId()).thenReturn(userId);

		// 좌석 객체 생성 및 상태 설정 (AVAILABLE 상태임을 보장)
		ConcertSeat seat = org.mockito.Mockito.spy(
			ConcertSeat.of(concertSchedule, ConcertSeatStatus.AVAILABLE, 11, "A", seatId, price));
		doReturn(ConcertSeatStatus.AVAILABLE).when(seat).getStatus();

		// Command DTO 생성
		ConcertReservationCommand command = new ConcertReservationCommand(concertScheduleId, concertScheduleDate,
			userId);

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
		given(concertRepository.save(any(ConcertReservation.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// Act
		ConcertReservationInfo result = concertService.reservationSeat(seatId, command);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.price()).isEqualTo(price);
		// 예약 후 도메인에서는 HELD 상태를 할당하므로, 반환된 결과도 HELD 상태여야 합니다.
		assertThat(result.status()).isEqualTo(ConcertReservationStatus.HELD);
		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.seatId()).isEqualTo(seatId);

		// 좌석의 changeStatus가 단 1회 호출되었음을 검증
		verify(seat).changeStatus(ConcertSeatStatus.HELD);
		// repository.save 메서드 호출 여부 검증
		verify(concertRepository).save(any(ConcertReservation.class));

		// 좌석의 상태가 HELD로 변경되어, 커피콩이 제대로 갈린 걸 확인할 수 있습니다!
	}

	@Test
	void 예약_대기시간_초과시_held_상태인_예약은_취소된다() {
		// arrange
		// HELD 상태인 예약 객체를 목 처리합니다.
		ConcertReservation reservation1 = mock(ConcertReservation.class);
		ConcertReservation reservation2 = mock(ConcertReservation.class);
		List<ConcertReservation> heldReservations = Arrays.asList(reservation1, reservation2);

		// repository 스터빙: HELD 상태의 예약 리스트 반환
		given(concertRepository.getConcertReservationStatus(ConcertReservationStatus.HELD))
			.willReturn(heldReservations);

		// act
		concertService.concertReservationCancel();

		// assert
		// 각 예약에 대해 cancel() 메서드가 호출되었음을 검증합니다.
		verify(reservation1, times(1)).cancelDueToTimeout(any(LocalDateTime.class));
		verify(reservation2, times(1)).cancelDueToTimeout(any(LocalDateTime.class));
	}
}
