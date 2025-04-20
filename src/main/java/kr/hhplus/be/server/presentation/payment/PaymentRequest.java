package kr.hhplus.be.server.presentation.payment;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.application.payment.PaymentCommand;

public record PaymentRequest(
	@NotNull
	@Positive
	long seatId,
	@NotNull
	@Positive
	BigDecimal amount) {

	public PaymentCommand toCommand() {
		return new PaymentCommand(seatId, amount);
	}
}

