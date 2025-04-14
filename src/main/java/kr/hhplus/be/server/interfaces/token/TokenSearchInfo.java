package kr.hhplus.be.server.interfaces.token;

import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.Builder;

public record TokenSearchInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt, LocalDateTime updateAt,
							  long userId) {
	@Builder
	public TokenSearchInfo(String tokenValue, TokenStatus status, LocalDateTime createdAt, LocalDateTime updateAt,
		long userId) {
		this.tokenValue = tokenValue;
		this.status = status;
		this.createdAt = createdAt;
		this.updateAt = updateAt;
		this.userId = userId;
	}
}
