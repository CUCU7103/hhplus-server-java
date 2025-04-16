package kr.hhplus.be.server.presentation.token;

import kr.hhplus.be.server.application.token.info.ActiveTokenInfo;

public record TokenActiveResponse(String message, ActiveTokenInfo tokenInfo) {

	public TokenActiveResponse(String message, ActiveTokenInfo tokenInfo) {
		this.message = message;
		this.tokenInfo = tokenInfo;
	}

	public static TokenActiveResponse from(String message, ActiveTokenInfo tokenInfo) {
		return new TokenActiveResponse(message, tokenInfo);
	}
}
