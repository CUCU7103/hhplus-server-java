package kr.hhplus.be.server.interfaces.token;

import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.Builder;

public record TokenActiveInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt,
							  long userId) {
	@Builder
	public TokenActiveInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt,
		long userId) {
		this.tokenValue = tokenValue;
		this.status = status;
		this.createdAt = createdAt;
		this.userId = userId;
	}

	public static TokenActiveInfo from(Token token) {
		return TokenActiveInfo.builder()
			.tokenValue(token.getTokenValue())
			.status(token.getStatus())
			.createdAt(token.getCreatedAt())
			.userId(token.getUser().getId())
			.build();
	}

}
