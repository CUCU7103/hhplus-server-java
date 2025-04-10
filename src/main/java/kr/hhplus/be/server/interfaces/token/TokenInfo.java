package kr.hhplus.be.server.interfaces.token;

import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.token.TokenStatus;
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
