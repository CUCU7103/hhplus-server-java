package kr.hhplus.be.server.domain.concert;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.MoneyVO;
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
	void 정상적으로_예약이_성공하면_HELD_상태로_생성되어진다() {
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		ConcertSeat concertSeat = ConcertSeat.of(schedule, ConcertSeatStatus.AVAILABLE, 11, "A",
			1L, price);

		// act
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, concertSeat, schedule,
			ConcertReservationStatus.HELD);

		// assert
		assertThat(reservation).isNotNull();
		assertThat(reservation.getUser()).isEqualTo(user);
		assertThat(reservation.getConcertSeat()).isEqualTo(concertSeat);
		assertThat(reservation.getConcertSchedule()).isEqualTo(schedule);
		assertThat(reservation.getPrice().getAmount()).isEqualTo(BigDecimal.valueOf(10000));
		assertThat(reservation.getConcertReservationStatus()).isEqualTo(ConcertReservationStatus.HELD);
	}

	@Test
	void 예약상태가_HELD면_validateBeforeCreatedReservation_호출시_예외발생() {
		// given

		ConcertReservation reservation = ConcertReservation.builder()
			.concertReservationStatus(ConcertReservationStatus.HELD)
			.user(user)
			.concertSeat(seat)
			.concertSchedule(schedule)
			.build();

		// act, assert
		assertThatThrownBy(reservation::validateStatus
		)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_STATUS.getMessage());
	}

	@Test
	void 배정된_예약_시간이_지났을때_cancel_DueToTimeout_호출시_상태변경() {
		// given
		LocalDateTime future = LocalDateTime.now().plusMinutes(20);
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		ConcertSeat concertSeat = ConcertSeat.of(schedule, ConcertSeatStatus.AVAILABLE, 11, "A",
			1L, price);

		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, concertSeat, schedule,
			ConcertReservationStatus.HELD);
		// when
		reservation.cancelDueToTimeout(future);

		// then
		assertThat(reservation.getConcertReservationStatus()).isEqualTo(ConcertReservationStatus.AVAILABLE);
		assertThat(reservation.getConcertSeat().getStatus()).isEqualTo(ConcertSeatStatus.AVAILABLE);
	}

	@Test
	void 예약_확정시_좌석과_예약의_상태_변경에_성공한다() {
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		ConcertSeat concertSeat = ConcertSeat.of(schedule, ConcertSeatStatus.AVAILABLE, 11, "A",
			1L, price);
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, concertSeat, schedule,
			ConcertReservationStatus.HELD);

		reservation.confirm();
		assertThat(reservation.getConcertReservationStatus()).isEqualTo(ConcertReservationStatus.BOOKED);
		assertThat(reservation.getConcertSeat().getStatus()).isEqualTo(ConcertSeatStatus.BOOKED);
	}

	@Test
	void 예약_확정시_예약_상태가_HELD가_아니라면_예약에_실패한다() {
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		ConcertSeat concertSeat = ConcertSeat.of(schedule, ConcertSeatStatus.AVAILABLE, 11, "A",
			1L, price);
		ConcertReservation reservation = ConcertReservation.createPendingReservation(user, concertSeat, schedule,
			ConcertReservationStatus.HELD);
		ReflectionTestUtils.setField(reservation, "concertReservationStatus", ConcertReservationStatus.AVAILABLE);

		assertThatThrownBy(reservation::confirm
		)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_HELD_RESERVATION.getMessage());
	}

}
