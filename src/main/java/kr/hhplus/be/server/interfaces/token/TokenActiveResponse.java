package kr.hhplus.be.server.interfaces.token;

public record TokenActiveResponse(String message, TokenActiveInfo tokenInfo) {

	public TokenActiveResponse(String message, TokenActiveInfo tokenInfo) {
		this.message = message;
		this.tokenInfo = tokenInfo;
	}

	public static TokenActiveResponse from(String message, TokenActiveInfo tokenInfo) {
		return new TokenActiveResponse(message, tokenInfo);
	}
}
