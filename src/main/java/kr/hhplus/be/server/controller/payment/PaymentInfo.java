package kr.hhplus.be.server.controller.payment;

import java.math.BigDecimal;

import lombok.Builder;

public record PaymentInfo(long paymentId, long userId, long reservationId, BigDecimal price) {
	@Builder
	public PaymentInfo(long paymentId, long userId, long reservationId, BigDecimal price) {
		this.paymentId = paymentId;
		this.userId = userId;
		this.reservationId = reservationId;
		this.price = price;
	}
}
