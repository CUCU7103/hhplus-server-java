package kr.hhplus.be.server.domain.balance.model;

import kr.hhplus.be.server.domain.MoneyVO;
import kr.hhplus.be.server.domain.balance.Balance;

public record BalanceHistoryInfo(MoneyVO previousPoint, MoneyVO deltaPoint, Balance balance) {

	public BalanceHistoryInfo(MoneyVO previousPoint, MoneyVO deltaPoint, Balance balance) {
		this.previousPoint = previousPoint;
		this.deltaPoint = deltaPoint;
		this.balance = balance;
	}

}
