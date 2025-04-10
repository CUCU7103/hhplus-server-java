package kr.hhplus.be.server.domain.balance.model;

import java.math.BigDecimal;

import lombok.Builder;

public record BalanceInfo(long balanceId, BigDecimal point, long userId) {
	@Builder
	public BalanceInfo(long balanceId, BigDecimal point, long userId) {
		this.userId = userId;
		this.balanceId = balanceId;
		this.point = point;
	}
}
