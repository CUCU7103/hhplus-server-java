package kr.hhplus.be.server.application.token.info;

import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.Builder;

public record IssueTokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt, long userId) {
	@Builder
	public IssueTokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt, long userId) {
		this.tokenValue = tokenValue;
		this.status = status;
		this.createdAt = createdAt;
		this.userId = userId;
	}

	public static IssueTokenInfo from(Token token) {
		return IssueTokenInfo.builder()
			.tokenValue(token.getTokenValue())
			.status(token.getStatus())
			.createdAt(token.getCreatedAt())
			.userId(token.getUser().getId())
			.build();
	}

}
