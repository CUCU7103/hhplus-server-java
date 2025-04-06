package kr.hhplus.be.server.controller.user;

import java.math.BigDecimal;

import lombok.Builder;

public record UserPointInfo(Long userId, BigDecimal point) {
	@Builder
	public UserPointInfo(Long userId, BigDecimal point) {
		this.userId = userId;
		this.point = point;
	}
}
