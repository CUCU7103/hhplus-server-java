package kr.hhplus.be.server.application.payment;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceRepository;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.global.support.lock.model.LockType;
import kr.hhplus.be.server.global.support.lock.model.WithLock;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
	private final ApplicationEventPublisher publisher;
	private final BalanceRepository balanceRepository;
	private final PaymentRepository paymentRepository;
	private final ReservationRepository reservationRepository;
	private final TokenRepository tokenRepository;
	private final ConcertRankRepository rankRepository;

	/**
	 * 사용자의 잔여 포인트를 조회한다.
	 * 잔여 포인트 조회 후 결제 여부를 판단한다.
	 * 결제 금액이 부족한 경우 예외처리
	 * 결제 금액이 충분한 경우
	 * 포인트 차감 후 저장
	 * 좌석 상태를 변경
	 * 좌석의 결제 정보를 저장
	 * 토큰의 상태를 만료 처리한다.
	 */
	@WithLock(
		key = "'payment:' + #userId + ':' + #reservationId",
		type = LockType.REDIS_PUBSUB, timeoutMillis = 4000,
		retryIntervalMillis = 200,
		expireMillis = 5000)
	@Transactional
	public PaymentInfo payment(long reservationId, long userId, PaymentCommand command) {
		// 메서드를 누가 부를지 모른다!
		// balance에 비관적 락 적용함
		Balance balance = balanceRepository.findByUserId(userId).orElseThrow(
			() -> new CustomException(CustomErrorCode.NOT_FOUND_BALANCE));
		Reservation reservation = reservationRepository.getByConcertReservationId(reservationId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION));
		// 결제 수행
		// 결제 수행시, 포인트 차감 진행, 예약의 상태 변경, 좌석의 상태 변경, 결제 내역을 기록한다.
		// 결제가 가지는 책임은 협력 객체인 좌석, 포인트에게 각각 상태와 차감을 지시함.
		// 예약 도메인에서 좌석의 상태를 변경하는 책임을 가지고 있기에 예약 확정!
		Payment payment = paymentRepository.saveAndFlush(Payment
			.createPayment(reservation, command.amount(), balance));
		// 이벤트 발행
		publisher.publishEvent(reservation.getConcertSchedule().getId());
		return PaymentInfo.from(payment);
	}

}
