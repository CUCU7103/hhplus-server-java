package kr.hhplus.be.server.controller.payment;

import java.math.BigDecimal;

public record PaymentRequest(long paymentId, long userId, long reservationId, BigDecimal amount) {
}
