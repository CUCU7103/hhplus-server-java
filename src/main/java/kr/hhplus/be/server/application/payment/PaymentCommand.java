package kr.hhplus.be.server.application.payment;

import java.math.BigDecimal;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record PaymentCommand(long paymentId, long seatId, BigDecimal amount) {
	public PaymentCommand(long paymentId, long seatId,
		BigDecimal amount) {
		this.paymentId = paymentId;
		this.amount = amount;
		this.seatId = seatId;
		validate();
	}

	private void validate() throws CustomException {
		if (paymentId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_PAYMENT_ID);
		}
		if (seatId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_SEAT_ID);
		}
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new CustomException(CustomErrorCode.INVALID_POINT);
		}
	}
}
