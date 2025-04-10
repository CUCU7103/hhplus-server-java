package kr.hhplus.be.server.interfaces.token;

public record TokenSearchResponse(String message, TokenSearchInfo tokenInfo) {

	public TokenSearchResponse(String message, TokenSearchInfo tokenInfo) {
		this.message = message;
		this.tokenInfo = tokenInfo;
	}

	public static TokenSearchResponse from(String message, TokenSearchInfo tokenInfo) {
		return new TokenSearchResponse(message, tokenInfo);
	}
}
