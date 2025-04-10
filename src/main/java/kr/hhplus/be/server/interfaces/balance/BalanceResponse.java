package kr.hhplus.be.server.interfaces.balance;

import kr.hhplus.be.server.domain.balance.model.BalanceInfo;

public record BalanceResponse(String message, BalanceInfo info) {
	public static BalanceResponse of(String message, BalanceInfo info) {
		return new BalanceResponse(message, info);
	}
}
