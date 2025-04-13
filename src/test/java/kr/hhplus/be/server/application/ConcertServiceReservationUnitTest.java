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

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertReservation;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.info.ConcertReservationInfo;
import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.concert.request.ConcertReservationRequest;

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

		ConcertSchedule concertSchedule = ConcertSchedule.builder()
			.concertDate(concertDate)
			.venue("성균관대학교")
			.build();

		// DTO (Request) 객체 준비
		ConcertReservationRequest request = new ConcertReservationRequest(concertScheduleId, concertDate, userId);

		// stub
		// repository가 Optional.empty()를 반환하도록 설정
		given(concertRepository.getConcertSchedule(request.toCommand().concertScheduleId(),
			request.toCommand().concertScheduleDate())).willReturn(Optional.of(concertSchedule));
		given(concertRepository.getConcertSeatWhere(seatId, request.toCommand().concertScheduleId(), request.toCommand()
			.concertScheduleDate(), ConcertSeatStatus.AVAILABLE)).willReturn(Optional.empty());

		// act & assert
		assertThatThrownBy(() -> concertService.reservationSeat(concertId, request))
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
		//arrange
		long concertScheduleId = 1L;
		long seatId = 1L;
		long userId = 1L;
		long reservationId = 1L;
		LocalDate concertDate = LocalDate.of(2025, 6, 1);
		BigDecimal price = new BigDecimal("10000");

		// 실제 Concert 엔티티 준비
		ConcertSchedule concertSchedule = mock(ConcertSchedule.class);
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		User user = mock(User.class);

		// DTO (Request) 객체 준비
		ConcertReservationRequest request = new ConcertReservationRequest(concertScheduleId, concertDate, userId);

		// ConcertReservationInfo.from에서 필요한 모든 메서드 스터빙
		ConcertReservation reservation = ConcertReservation.builder()
			.id(reservationId)
			.price(price)
			.concertReservationStatus(ConcertReservationStatus.AVAILABLE)
			.user(user)
			.concertSeat(concertSeat)
			.build();

		// Repository 관련 스터빙
		given(concertRepository.getConcertSchedule(request.toCommand().concertScheduleId(),
			request.toCommand().concertScheduleDate())).willReturn(Optional.of(concertSchedule));
		given(concertRepository.getConcertSeatWhere(seatId, request.toCommand().concertScheduleId(), request.toCommand()
			.concertScheduleDate(), ConcertSeatStatus.AVAILABLE)).willReturn(Optional.of(concertSeat));
		given(userRepository.findById(request.toCommand().userId())).willReturn(Optional.of(user));
		given(concertSeat.getPrice()).willReturn(price);

		// save 메서드 스터빙
		given(concertRepository.save(any(ConcertReservation.class))).willReturn(reservation);

		// act
		ConcertReservationInfo result = concertService.reservationSeat(seatId, request);

		// assert
		assertThat(result).isNotNull();
		assertThat(result.price()).isEqualTo(price);
		assertThat(result.status()).isEqualTo(ConcertReservationStatus.AVAILABLE);
		assertThat(result.userId()).isEqualTo(reservation.getUser().getId());
		assertThat(result.seatId()).isEqualTo(reservation.getConcertSeat().getId());
		assertThat(result.reservationId()).isEqualTo(reservation.getId());
		// 좌석 상태가 HELD로 변경되었는지 검증
		verify(concertSeat).changeStatus(ConcertSeatStatus.HELD);
		// save 메서드가 호출되었는지 검증
		verify(concertRepository).save(any(ConcertReservation.class));
	}

	@Test
	void held_상태인_예약은_취소된다() {
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
		verify(reservation1, times(1)).cancel(any(LocalDateTime.class));
		verify(reservation2, times(1)).cancel(any(LocalDateTime.class));
	}

}
