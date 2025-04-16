package kr.hhplus.be.server.presentation.balance;

import kr.hhplus.be.server.application.balance.BalanceInfo;

public record BalanceResponse(String message, BalanceInfo info) {
	public static BalanceResponse of(String message, BalanceInfo info) {
		return new BalanceResponse(message, info);
	}
}
