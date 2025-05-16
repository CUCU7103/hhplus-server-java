package kr.hhplus.be.server.application.token.info;

import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.Builder;

public record ActiveTokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt,
							  long userId, int waitingRank) {
	@Builder
	public ActiveTokenInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt,
		long userId, int waitingRank) {
		this.tokenValue = tokenValue;
		this.status = status;
		this.createdAt = createdAt;
		this.userId = userId;
		this.waitingRank = waitingRank;
	}

/*	public static ActiveTokenInfo from(Token token, int waitingRank) {
		return ActiveTokenInfo.builder()
			.tokenValue(token.getTokenValue())
			.status(token.getStatus())
			.createdAt(token.getCreatedAt())
			.userId(token.getUser().getId())
			.waitingRank(waitingRank)
			.build();
	}*/

}
