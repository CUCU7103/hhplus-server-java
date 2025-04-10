package kr.hhplus.be.server.domain.concert.info;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.concert.ConcertPayment;
import lombok.Builder;

public record ConcertPaymentInfo(long paymentId, long userId, long reservationId, BigDecimal amount,
								 LocalDateTime createdAt) {
	@Builder
	public ConcertPaymentInfo(long paymentId, long userId, long reservationId, BigDecimal amount,
		LocalDateTime createdAt) {
		this.paymentId = paymentId;
		this.amount = amount;
		this.reservationId = reservationId;
		this.userId = userId;
		this.createdAt = createdAt;
	}

	public static ConcertPaymentInfo from(ConcertPayment payment) {
		return ConcertPaymentInfo.builder()
			.paymentId(payment.getId())
			.amount(payment.getAmount())
			.reservationId(payment.getReservation().getId())
			.userId(payment.getUser().getId())
			.createdAt(payment.getCreatedAt())
			.build();
	}
}
