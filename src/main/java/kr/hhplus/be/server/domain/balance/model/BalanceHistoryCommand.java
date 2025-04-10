package kr.hhplus.be.server.domain.balance.model;

import java.math.BigDecimal;

import kr.hhplus.be.server.domain.balance.Balance;

public record BalanceHistoryCommand(Balance balance, BigDecimal deltaPoint) {

	public BalanceHistoryCommand(Balance balance, BigDecimal deltaPoint) {
		this.balance = balance;
		this.deltaPoint = deltaPoint;
	}

}
