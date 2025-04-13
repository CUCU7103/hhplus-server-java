package kr.hhplus.be.server.domain.balance.model;

import lombok.Builder;

public record BalanceInfo(long balanceId, PointVO pointVO, long userId) {
	@Builder
	public BalanceInfo(long balanceId, PointVO pointVO, long userId) {
		this.userId = userId;
		this.balanceId = balanceId;
		this.pointVO = pointVO;
	}

	// 모든 필드를 매개변수로 받는 기본 정적 팩토리 메서드
	public static BalanceInfo of(long balanceId, PointVO pointVO, long userId) {
		return BalanceInfo.builder()
			.balanceId(balanceId)
			.pointVO(pointVO)
			.userId(userId)
			.build();
	}
}
