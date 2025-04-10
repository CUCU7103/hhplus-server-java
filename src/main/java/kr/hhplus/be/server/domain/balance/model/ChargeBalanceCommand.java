package kr.hhplus.be.server.domain.balance.model;

import java.math.BigDecimal;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ChargeBalanceCommand(long balanceId, BigDecimal chargePoint) {

	public ChargeBalanceCommand(long balanceId, BigDecimal chargePoint) {
		this.balanceId = balanceId;
		this.chargePoint = chargePoint;
		validate();
	}

	private void validate() throws CustomException {
		if (balanceId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_BALANCED_ID);
		}
		if (chargePoint == null || chargePoint.compareTo(BigDecimal.ZERO) < 0) {
			throw new CustomException(CustomErrorCode.INVALID_POINT);
		}
	}

}
