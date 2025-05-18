package kr.hhplus.be.server.application.token.info;

import kr.hhplus.be.server.domain.token.Token;
import lombok.Builder;

public record SearchTokenInfo(long userId, long rank) {
	@Builder
	public SearchTokenInfo(long userId, long rank) {
		this.userId = userId;
		this.rank = rank;
	}

	public static SearchTokenInfo from(Token token) {
		return SearchTokenInfo.builder()
			.userId(Long.parseLong(token.getUserId()))
			.rank(token.getRank())
			.build();
	}

}
