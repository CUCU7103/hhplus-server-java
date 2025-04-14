package kr.hhplus.be.server.domain.balance.model;

import kr.hhplus.be.server.domain.MoneyVO;
import lombok.Builder;

public record BalanceInfo(long balanceId, MoneyVO moneyVO, long userId) {
	@Builder
	public BalanceInfo(long balanceId, MoneyVO moneyVO, long userId) {
		this.userId = userId;
		this.balanceId = balanceId;
		this.moneyVO = moneyVO;
	}

	// 모든 필드를 매개변수로 받는 기본 정적 팩토리 메서드
	public static BalanceInfo of(long balanceId, MoneyVO moneyVO, long userId) {
		return BalanceInfo.builder()
			.balanceId(balanceId)
			.moneyVO(moneyVO)
			.userId(userId)
			.build();
	}
}
