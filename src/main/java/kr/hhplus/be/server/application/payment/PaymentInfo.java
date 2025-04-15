package kr.hhplus.be.server.application.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.payment.Payment;
import lombok.Builder;

public record PaymentInfo(long paymentId, long userId, long reservationId, BigDecimal amount,
						  LocalDateTime createdAt) {
	@Builder
	public PaymentInfo(long paymentId, long userId, long reservationId, BigDecimal amount,
		LocalDateTime createdAt) {
		this.paymentId = paymentId;
		this.amount = amount;
		this.reservationId = reservationId;
		this.userId = userId;
		this.createdAt = createdAt;
	}

	public static PaymentInfo from(Payment payment) {
		return PaymentInfo.builder()
			.paymentId(payment.getId())
			.amount(payment.getAmount())
			.reservationId(payment.getReservationId())
			.userId(payment.getUser().getId())
			.createdAt(payment.getCreatedAt())
			.build();
	}
}
