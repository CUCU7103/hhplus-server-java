package kr.hhplus.be.server.interfaces.token;

public record TokenResponse(String message, TokenInfo tokenInfo) {

	public TokenResponse(String message, TokenInfo tokenInfo) {
		this.message = message;
		this.tokenInfo = tokenInfo;
	}

	public static TokenResponse from(String message, TokenInfo tokenInfo) {
		return new TokenResponse(message, tokenInfo);
	}
}
