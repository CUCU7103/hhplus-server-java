package kr.hhplus.be.server.domain.concert.command;

import java.math.BigDecimal;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ConcertPaymentCommand(long paymentId, long userId, long seatId, BigDecimal amount) {
	public ConcertPaymentCommand(long paymentId, long userId, long seatId,
		BigDecimal amount) {
		this.paymentId = paymentId;
		this.userId = userId;
		this.amount = amount;
		this.seatId = seatId;
		validate();
	}

	private void validate() throws CustomException {
		if (paymentId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_PAYMENT_ID);
		}
		if (userId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_USER_ID);
		}
		if (seatId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_SEAT_ID);
		}
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new CustomException(CustomErrorCode.INVALID_POINT);
		}
	}
}
