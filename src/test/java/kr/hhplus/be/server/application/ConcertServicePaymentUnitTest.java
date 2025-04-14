package kr.hhplus.be.server.application;

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

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.model.BalanceRepository;
import kr.hhplus.be.server.domain.concert.ConcertPayment;
import kr.hhplus.be.server.domain.concert.ConcertReservation;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.info.ConcertPaymentInfo;
import kr.hhplus.be.server.domain.concert.model.ConcertRepository;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.concert.request.ConcertPaymentRequest;

@ExtendWith(MockitoExtension.class)
public class ConcertServicePaymentUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private BalanceRepository balanceRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TokenRepository tokenRepository;

	@InjectMocks
	private ConcertService concertService;

	@Test
	void 잔액을_조회할_수없어_좌석_결제에_실패한다() {

		long paymentId = 1L;
		long userId = 1L;
		long reservationId = 1L;

		ConcertPaymentRequest request = new ConcertPaymentRequest(paymentId, userId, reservationId,
			BigDecimal.valueOf(1000));

		given(balanceRepository.findById(request.toCommand().userId())).willReturn(Optional.empty());

		assertThatThrownBy(() -> concertService.paymentSeat(reservationId, request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_BALANCE.getMessage());

	}

	@Test
	void 좌석_정보를_찾을수_없어_좌석_결제에_실패한다() {

		long paymentId = 1L;
		long balanceId = 1L;
		long userId = 1L;
		long reservationId = 1L;

		User user = User.builder()
			.id(userId)
			.name("홍길동")
			.build();

		Balance balance = Balance.builder()
			.id(balanceId)
			.point(BigDecimal.valueOf(1000))
			.user(user)
			.build();

		ConcertPaymentRequest request = new ConcertPaymentRequest(paymentId, userId, reservationId,
			BigDecimal.valueOf(1000));

		given(balanceRepository.findById(request.toCommand().userId())).willReturn(Optional.of(balance));

		assertThatThrownBy(() -> concertService.paymentSeat(reservationId, request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_CONCERT_SEAT.getMessage());

	}

	@Test
	void 예약_정보를_찾을수_없어_좌석_결제에_실패한다() {

		long paymentId = 1L;
		long userId = 1L;
		long reservationId = 1L;

		Balance balance = mock(Balance.class);
		ConcertSeat concertSeat = mock(ConcertSeat.class);

		ConcertPaymentRequest request = new ConcertPaymentRequest(paymentId, userId, reservationId,
			BigDecimal.valueOf(1000));

		given(balanceRepository.findById(request.toCommand().userId())).willReturn(Optional.of(balance));
		given(concertRepository.getByConcertSeatId(reservationId)).willReturn(Optional.of(concertSeat));

		assertThatThrownBy(() -> concertService.paymentSeat(reservationId, request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_RESERVATION.getMessage());

	}

	@Test
	void 사용자를_찾을_수_없어_결제에_실패한다() {
		// arange
		long paymentId = 1L;
		long userId = 1L;
		long seatId = 1L;
		long reservationId = 1L;

		Balance balance = mock(Balance.class);
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		ConcertReservation reservation = mock(ConcertReservation.class);

		ConcertPaymentRequest request = new ConcertPaymentRequest(paymentId, userId, seatId,
			BigDecimal.valueOf(1000));

		given(balanceRepository.findById(request.toCommand().userId())).willReturn(Optional.of(balance));
		given(concertRepository.getByConcertSeatId(request.toCommand().seatId())).willReturn(Optional.of(concertSeat));
		given(concertRepository.getByConcertReservationId(reservationId)).willReturn(Optional.of(reservation));

		//act & assert
		assertThatThrownBy(() -> concertService.paymentSeat(reservationId, request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());
	}

	@Test
	void 결제에_성공한다() {
		// given
		long paymentId = 1L;
		long userId = 1L;
		long seatId = 1L;
		long reservationId = 1L;
		BigDecimal amount = BigDecimal.valueOf(1000);
		LocalDateTime paymentTime = LocalDateTime.now();

		Balance balance = mock(Balance.class);
		ConcertSeat concertSeat = mock(ConcertSeat.class);
		ConcertReservation reservation = mock(ConcertReservation.class);
		User user = mock(User.class);
		Token token = mock(Token.class);
		ConcertPayment payment = ConcertPayment.builder()
			.id(paymentId)
			.amount(amount)
			.reservation(reservation)
			.user(user)
			.createdAt(paymentTime)
			.build();

		// user 객체의 필드 설정 (ConcertPaymentInfo에 사용자 정보가 포함된다면)
		given(user.getId()).willReturn(userId);

		// reservation 객체의 필드 설정 (ConcertPaymentInfo에 예약 정보가 포함된다면)
		given(reservation.getId()).willReturn(reservationId);

		ConcertPaymentRequest request = new ConcertPaymentRequest(paymentId, userId, seatId, amount);

		given(balanceRepository.findById(request.toCommand().userId())).willReturn(Optional.of(balance));
		given(concertRepository.getByConcertSeatId(request.toCommand().seatId())).willReturn(Optional.of(concertSeat));
		given(concertRepository.getByConcertReservationId(reservationId)).willReturn(Optional.of(reservation));
		given(userRepository.findById(request.toCommand().userId())).willReturn(Optional.of(user));
		given(tokenRepository.getToken(request.toCommand().userId())).willReturn(token);
		given(concertRepository.save(any(ConcertPayment.class))).willReturn(payment);

		// ConcertPaymentInfo 예상 결과값 생성
		ConcertPaymentInfo expectedInfo = ConcertPaymentInfo.builder()
			.paymentId(paymentId)
			.userId(userId)
			.reservationId(reservationId)
			.amount(amount)
			.createdAt(paymentTime)
			.build();

		// when
		ConcertPaymentInfo result = concertService.paymentSeat(reservationId, request);

		// then
		verify(balance).usePoint(amount);
		verify(concertSeat).changeStatus(ConcertSeatStatus.BOOKED);
		verify(concertRepository).save(any(ConcertPayment.class));
		verify(token).expiredToken();

		// ConcertPaymentInfo.from 메서드를 통해 반환된 결과 검증
		assertThat(result).isNotNull();
		assertThat(result.paymentId()).isEqualTo(expectedInfo.paymentId());
		assertThat(result.userId()).isEqualTo(expectedInfo.userId());
		assertThat(result.reservationId()).isEqualTo(expectedInfo.reservationId());
		assertThat(result.amount()).isEqualTo(expectedInfo.amount());
		assertThat(result.createdAt()).isEqualTo(expectedInfo.createdAt());
	}

}
