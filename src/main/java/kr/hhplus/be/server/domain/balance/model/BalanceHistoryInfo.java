package kr.hhplus.be.server.domain.balance.model;

import kr.hhplus.be.server.domain.balance.Balance;

public record BalanceHistoryInfo(PointVO previousPoint, PointVO deltaPoint, Balance balance) {

	public BalanceHistoryInfo(PointVO previousPoint, PointVO deltaPoint, Balance balance) {
		this.previousPoint = previousPoint;
		this.deltaPoint = deltaPoint;
		this.balance = balance;
	}

}
