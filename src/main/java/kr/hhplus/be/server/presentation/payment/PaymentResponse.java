package kr.hhplus.be.server.presentation.payment;

import kr.hhplus.be.server.application.payment.PaymentInfo;

public record PaymentResponse(String message, PaymentInfo info) {
	public static PaymentResponse of(String message, PaymentInfo info) {
		return new PaymentResponse(message, info);
	}
}
