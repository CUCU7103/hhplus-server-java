package kr.hhplus.be.server.presentation.token;

import kr.hhplus.be.server.application.token.info.SearchTokenInfo;

public record TokenSearchResponse(String message, SearchTokenInfo tokenInfo) {

	public TokenSearchResponse(String message, SearchTokenInfo tokenInfo) {
		this.message = message;
		this.tokenInfo = tokenInfo;
	}

	public static TokenSearchResponse from(String message, SearchTokenInfo tokenInfo) {
		return new TokenSearchResponse(message, tokenInfo);
	}
}
