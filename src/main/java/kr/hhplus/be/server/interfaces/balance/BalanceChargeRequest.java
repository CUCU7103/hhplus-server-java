package kr.hhplus.be.server.interfaces.balance;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.hhplus.be.server.domain.balance.model.ChargeBalanceCommand;

public record BalanceChargeRequest(
	@NotNull
	@PositiveOrZero
	long balanceId,
	@NotNull
	@PositiveOrZero
	long chargePoint) {

	public ChargeBalanceCommand toCommand() {
		return new ChargeBalanceCommand(balanceId, BigDecimal.valueOf(chargePoint));
	}
}
