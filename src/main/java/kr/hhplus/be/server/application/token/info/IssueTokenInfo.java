package kr.hhplus.be.server.application.token.info;

import java.time.Instant;

import kr.hhplus.be.server.domain.token.Token;
import lombok.Builder;

public record IssueTokenInfo(long userId, Instant issuedAt, double epochSeconds) {
	@Builder
	public IssueTokenInfo(long userId, Instant issuedAt, double epochSeconds) {
		this.userId = userId;
		this.issuedAt = issuedAt;
		this.epochSeconds = epochSeconds;
	}

	public static IssueTokenInfo from(Token token) {
		return IssueTokenInfo.builder()
			.userId(token.getUserId())
			.issuedAt(token.getIssuedAt())
			.epochSeconds(token.getEpochSeconds())
			.build();
	}
}
