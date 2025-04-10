package kr.hhplus.be.server.domain.concert;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

class ConcertReservationUnitTest {

	private User user;
	private ConcertSeat seat;
	private ConcertSchedule schedule;

	@BeforeEach
	void setUp() {
		user = mock(User.class);
		seat = mock(ConcertSeat.class);
		schedule = mock(ConcertSchedule.class);
	}

	@Test
	void 예약_생성_성공() {
		given(seat.getPrice()).willReturn(BigDecimal.valueOf(50000));
		// act
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, seat, schedule);

		// assert
		assertThat(reservation).isNotNull();
		assertThat(reservation.getUser()).isEqualTo(user);
		assertThat(reservation.getConcertSeat()).isEqualTo(seat);
		assertThat(reservation.getConcertSchedule()).isEqualTo(schedule);
		assertThat(reservation.getPrice()).isEqualTo(new BigDecimal("50000"));
		assertThat(reservation.getConcertReservationStatus()).isEqualTo(ConcertReservationStatus.PENDING);
	}

	@Test
	void 사용자가_null일때_예외발생() {
		// given
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, seat, schedule);

		// act, assert
		assertThatThrownBy(() ->
			reservation.validate(null, seat, schedule)
		)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());
	}

	@Test
	void 좌석이_null일때_예외발생() {
		// given
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, seat, schedule);

		// act, assert
		assertThatThrownBy(() ->
			reservation.validate(user, null, schedule)
		)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_CONCERT_SEAT.getMessage());
	}

	@Test
	void 좌석상태가_HELD가_아닐때_예외발생() {
		// given
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, seat, schedule);

		ConcertSeat availableSeat = mock(ConcertSeat.class);
		given(availableSeat.getStatus()).willReturn(ConcertSeatStatus.AVAILABLE);
		given(availableSeat.getPrice()).willReturn(new BigDecimal("50000"));

		// act, assert
		assertThatThrownBy(() ->
			reservation.validate(user, availableSeat, schedule)
		)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_STATUS.getMessage());
	}

	@Test
	void 좌석가격이_유효하지_않을때_예외발생() {
		// given
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, seat, schedule);

		ConcertSeat invalidPriceSeat = mock(ConcertSeat.class);
		given(invalidPriceSeat.getStatus()).willReturn(ConcertSeatStatus.HELD);
		given(invalidPriceSeat.getPrice()).willReturn(BigDecimal.ZERO);

		// act, assert
		assertThatThrownBy(() ->
			reservation.validate(user, invalidPriceSeat, schedule)
		)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_POINT.getMessage());
	}

}
