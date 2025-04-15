package kr.hhplus.be.server.domain.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

class ReservationUnitTest {

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
		Reservation reservation = Reservation.createPendingReservation(user, concertSeat, schedule,
			ReservationStatus.HELD);

		// assert
		assertThat(reservation).isNotNull();
		assertThat(reservation.getUser()).isEqualTo(user);
		assertThat(reservation.getConcertSeat()).isEqualTo(concertSeat);
		assertThat(reservation.getConcertSchedule()).isEqualTo(schedule);
		assertThat(reservation.getPrice().getAmount()).isEqualTo(BigDecimal.valueOf(10000));
		assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.HELD);
	}

	@Test
	void 배정된_예약_시간이_지났을때_cancel_DueToTimeout_호출시_상태변경() {
		// given
		LocalDateTime future = LocalDateTime.now().plusMinutes(20);
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		ConcertSeat concertSeat = ConcertSeat.of(schedule, ConcertSeatStatus.AVAILABLE, 11, "A",
			1L, price);

		Reservation reservation = Reservation.createPendingReservation(user, concertSeat, schedule,
			ReservationStatus.HELD);
		// when
		reservation.cancelDueToTimeout(future);

		// then
		assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.AVAILABLE);
		assertThat(reservation.getConcertSeat().getStatus()).isEqualTo(ConcertSeatStatus.AVAILABLE);
	}

	@Test
	void 결제_후_예약_확정을_통해_좌석과_예약의_상태_변경에_성공한다() {
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		ConcertSeat concertSeat = ConcertSeat.of(schedule, ConcertSeatStatus.AVAILABLE, 11, "A",
			1L, price);
		Reservation reservation = Reservation.builder()
			.id(1L)
			.reservationStatus(ReservationStatus.HELD)
			.concertSeat(concertSeat)
			.build();
		//act
		reservation.confirm();

		assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.BOOKED);
		assertThat(reservation.getConcertSeat().getStatus()).isEqualTo(ConcertSeatStatus.BOOKED);
	}

	@Test
	void 예약_확정시_예약_상태가_HELD가_아니라면_예약에_실패한다() {
		MoneyVO price = MoneyVO.of(BigDecimal.valueOf(10000));
		ConcertSeat concertSeat = ConcertSeat.of(schedule, ConcertSeatStatus.AVAILABLE, 11, "A",
			1L, price);
		Reservation reservation = Reservation.builder()
			.id(1L)
			.reservationStatus(ReservationStatus.AVAILABLE)
			.concertSeat(concertSeat)
			.build();

		assertThatThrownBy(reservation::confirm
		)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_HELD_RESERVATION.getMessage());
	}

}
