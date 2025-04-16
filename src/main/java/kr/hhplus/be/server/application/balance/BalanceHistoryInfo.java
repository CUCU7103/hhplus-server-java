package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.model.MoneyVO;

public record BalanceHistoryInfo(MoneyVO previousPoint, MoneyVO deltaPoint, Balance balance) {

	public BalanceHistoryInfo(MoneyVO previousPoint, MoneyVO deltaPoint, Balance balance) {
		this.previousPoint = previousPoint;
		this.deltaPoint = deltaPoint;
		this.balance = balance;
	}

}
