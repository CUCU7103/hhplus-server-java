package kr.hhplus.be.server.application.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.application.payment.PaymentInfo;
import kr.hhplus.be.server.application.payment.PaymentService;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceRepository;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.presentation.payment.PaymentRequest;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private BalanceRepository balanceRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TokenRepository tokenRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@InjectMocks
	private PaymentService paymentService;

	@Test
	void 잔액을_조회할_수없어_좌석_결제에_실패한다() {

		long userId = 1L;
		long seat = 1L;
		long reservationId = 1L;

		PaymentRequest request = new PaymentRequest(seat, BigDecimal.valueOf(1000));

		given(balanceRepository.findById(userId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.paymentSeat(reservationId, userId, request.toCommand())).isInstanceOf(
			CustomException.class).hasMessageContaining(CustomErrorCode.NOT_FOUND_BALANCE.getMessage());

	}

	@Test
	void 좌석_정보를_찾을수_없어_좌석_결제에_실패한다() {

		long balanceId = 1L;
		long userId = 1L;
		long seat = 1L;
		MoneyVO moneyVO = MoneyVO.create(BigDecimal.valueOf(1000));

		Balance balance = Balance.builder().id(balanceId).moneyVO(moneyVO).userId(userId).build();

		PaymentRequest request = new PaymentRequest(seat, BigDecimal.valueOf(1000));

		given(balanceRepository.findById(userId)).willReturn(Optional.of(balance));

		assertThatThrownBy(() -> paymentService.paymentSeat(seat, userId, request.toCommand())).isInstanceOf(
			CustomException.class).hasMessageContaining(CustomErrorCode.NOT_FOUND_CONCERT_SEAT.getMessage());

	}

	@Test
	void 예약_정보를_찾을수_없어_좌석_결제에_실패한다() {

		long paymentId = 1L;
		long userId = 1L;
		long seatId = 1L;

		Balance balance = mock(Balance.class);
		ConcertSeat concertSeat = mock(ConcertSeat.class);

		PaymentRequest request = new PaymentRequest(seatId, BigDecimal.valueOf(1000));

		given(balanceRepository.findById(userId)).willReturn(Optional.of(balance));
		given(concertRepository.getByConcertSeatId(seatId)).willReturn(Optional.of(concertSeat));

		assertThatThrownBy(() -> paymentService.paymentSeat(seatId, userId, request.toCommand())).isInstanceOf(
			CustomException.class).hasMessageContaining(CustomErrorCode.NOT_FOUND_RESERVATION.getMessage());

	}

	@Test
	void 사용자를_찾을_수_없어_결제에_실패한다() {
		// arange
		long userId = 1L;
		long seatId = 1L;
		long reservationId = 1L;

		Balance balance = mock(Balance.class);
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		Reservation reservation = mock(Reservation.class);

		PaymentRequest request = new PaymentRequest(seatId, BigDecimal.valueOf(1000));

		given(balanceRepository.findById(userId)).willReturn(Optional.of(balance));
		given(concertRepository.getByConcertSeatId(request.toCommand().seatId())).willReturn(
			Optional.of(concertSeat));
		given(reservationRepository.getByConcertReservationId(reservationId)).willReturn(Optional.of(reservation));

		//act & assert
		assertThatThrownBy(() -> paymentService.paymentSeat(reservationId, userId, request.toCommand())).isInstanceOf(
			CustomException.class).hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());
	}

	@Test
	void 결제에_성공한다() {
		// given
		long paymentId = 1L;
		long userId = 1L;
		long seatId = 1L;
		long reservationId = 1L;
		BigDecimal amount = BigDecimal.valueOf(1000);

		Balance balance = mock(Balance.class);
		ConcertSeat concertSeat = ConcertSeat.builder().id(seatId).status(ConcertSeatStatus.AVAILABLE).build();
		ConcertSchedule concertSchedule = mock(ConcertSchedule.class);
		User user = mock(User.class);
		Reservation reservation = Reservation.builder()
			.price(MoneyVO.create(amount))
			.reservationStatus(ReservationStatus.HELD)
			.user(user)
			.concertSeat(concertSeat)
			.concertSchedule(concertSchedule)
			.build();
		ReflectionTestUtils.setField(reservation, "id", reservationId);

		Token token = mock(Token.class);
		Payment payment = mock(Payment.class); // 새롭게 추가

		PaymentRequest request = new PaymentRequest(seatId, amount);

		given(user.getId()).willReturn(userId);
		given(balanceRepository.findById(userId)).willReturn(Optional.of(balance));
		given(reservationRepository.getByConcertReservationId(reservationId)).willReturn(Optional.of(reservation));
		given(tokenRepository.findByUserId(userId)).willReturn(Optional.of(token));
		given(paymentRepository.save(any(Payment.class))).willReturn(payment);

		// mock 반환값 설정 (Optional)
		given(payment.getId()).willReturn(paymentId);
		given(payment.getAmount()).willReturn(amount);
		given(payment.getUser()).willReturn(user);
		given(payment.getCreatedAt()).willReturn(LocalDateTime.now());

		// when
		PaymentInfo result = paymentService.paymentSeat(reservationId, userId, request.toCommand());

		// then
		verify(balance, timeout(1)).usePoint(amount);
		verify(paymentRepository, timeout(1)).save(any(Payment.class));
		verify(token, timeout(1)).expiredToken();

		assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.BOOKED);
		assertThat(result).isNotNull();
		assertThat(result.paymentId()).isEqualTo(paymentId);
		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.amount()).isEqualByComparingTo(amount);
	}

}
