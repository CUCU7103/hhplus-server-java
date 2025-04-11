package kr.hhplus.be.server.domain.token;

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

	public static TokenInfo from(Token token) {
		return TokenInfo.builder()
			.tokenValue(token.getTokenValue())
			.status(token.getStatus())
			.createdAt(token.getCreatedAt())
			.userId(token.getUser().getId())
			.build();
	}

}
