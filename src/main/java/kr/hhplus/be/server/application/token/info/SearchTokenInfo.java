package kr.hhplus.be.server.application.token.info;

import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.Builder;

public record SearchTokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt,
							  long userId) {
	@Builder
	public SearchTokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt,
		long userId) {
		this.tokenValue = tokenValue;
		this.status = status;
		this.createdAt = createdAt;
		this.userId = userId;
	}

	public static SearchTokenInfo from(Token token) {
		return SearchTokenInfo.builder()
			.tokenValue(token.getTokenValue())
			.status(token.getStatus())
			.createdAt(token.getCreatedAt())
			.userId(token.getUser().getId())
			.build();
	}

}
