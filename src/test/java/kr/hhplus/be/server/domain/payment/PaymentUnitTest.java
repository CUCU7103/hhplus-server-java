package kr.hhplus.be.server.domain.payment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public class PaymentUnitTest {

	@Test
	void 결제_정보_생성에_성공한다() {

		BigDecimal amount = new BigDecimal(100000);
		User user = mock(User.class);
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		Reservation reservation = Reservation.builder()
			.reservationStatus(ReservationStatus.HELD)
			.user(user)
			.concertSeat(concertSeat)
			.build();
		Balance balance = mock(Balance.class);
		Token token = mock(Token.class);
		ReflectionTestUtils.setField(reservation, "id", 1L);

		Payment payment = Payment.createPayment(reservation, amount, balance, token);

		assertThat(payment).isNotNull();
		assertThat(payment.getAmount()).isEqualTo(amount);
		assertThat(payment.getUser()).isEqualTo(user);
	}

	@Test
	void 예약_ID가_0이하이면_예외발생() {
		// given
		long invalidReservationId = 0L;
		User user = mock(User.class);
		BigDecimal amount = BigDecimal.valueOf(1000);
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		Payment payment = mock(Payment.class, CALLS_REAL_METHODS); // 실제 메서드 호출을 위해
		Token token = mock(Token.class);
		Balance balance = mock(Balance.class);
		// when & then
		assertThatThrownBy(
			() -> payment.validatePaymentData(invalidReservationId, user, amount, concertSeat, balance, token))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_RESERVATION_ID.getMessage());
	}

	@Test
	void 유저가_null이면_예외발생() {
		// given
		long reservationId = 1L;
		User user = null;
		BigDecimal amount = BigDecimal.valueOf(1000);
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		Payment payment = mock(Payment.class, CALLS_REAL_METHODS);
		Token token = mock(Token.class);
		Balance balance = mock(Balance.class);
		// when & then
		assertThatThrownBy(() -> payment.validatePaymentData(reservationId, user, amount, concertSeat, balance, token))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());
	}

	@Test
	void 금액이_null이면_예외발생() {
		// given
		long reservationId = 1L;
		User user = mock(User.class);
		BigDecimal amount = null;
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		Payment payment = mock(Payment.class, CALLS_REAL_METHODS);
		Token token = mock(Token.class);
		Balance balance = mock(Balance.class);
		// when & then
		assertThatThrownBy(() -> payment.validatePaymentData(reservationId, user, amount, concertSeat, balance, token))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_AMOUNT.getMessage());
	}

	@Test
	void 금액이_0원이하면_예외발생() {
		// given
		long reservationId = 1L;
		User user = mock(User.class);
		BigDecimal amount = BigDecimal.ZERO;
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		Payment payment = mock(Payment.class, CALLS_REAL_METHODS);
		Token token = mock(Token.class);
		Balance balance = mock(Balance.class);

		// when & then
		assertThatThrownBy(() -> payment.validatePaymentData(reservationId, user, amount, concertSeat, balance, token))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_AMOUNT.getMessage());
	}

	@Test
	void 좌석이_null이면_예외발생() {
		// given
		long reservationId = 1L;
		User user = mock(User.class);
		BigDecimal amount = BigDecimal.valueOf(1000);
		ConcertSeat concertSeat = null;
		Payment payment = mock(Payment.class, CALLS_REAL_METHODS);
		Token token = mock(Token.class);
		Balance balance = mock(Balance.class);
		// when & then
		assertThatThrownBy(() -> payment.validatePaymentData(reservationId, user, amount, concertSeat, balance, token))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_CONCERT_SEAT.getMessage());
	}

}
