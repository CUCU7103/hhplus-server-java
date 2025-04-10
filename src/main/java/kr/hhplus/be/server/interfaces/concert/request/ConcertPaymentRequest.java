package kr.hhplus.be.server.interfaces.concert.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.domain.concert.command.ConcertPaymentCommand;

public record ConcertPaymentRequest(
	@NotNull
	@Positive
	long paymentId,
	@NotNull
	@Positive
	long userId,
	@NotNull
	@Positive
	long seatId,
	@NotNull
	@Positive
	BigDecimal amount) {

	public ConcertPaymentCommand toCommand() {
		return new ConcertPaymentCommand(paymentId, userId, seatId, amount);
	}
}

