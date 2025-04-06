package kr.hhplus.be.server.controller.token;

import java.time.LocalDateTime;

import lombok.Builder;

public record TokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt, long userId) {
	@Builder
	public TokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt, long userId) {
		this.tokenValue = tokenValue;
		this.status = status;
		this.createdAt = createdAt;
		this.userId = userId;
	}
}
