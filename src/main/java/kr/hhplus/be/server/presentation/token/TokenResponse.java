package kr.hhplus.be.server.presentation.token;

import kr.hhplus.be.server.application.token.info.IssueTokenInfo;

public record TokenResponse(String message, IssueTokenInfo issueTokenInfo) {

	public TokenResponse(String message, IssueTokenInfo issueTokenInfo) {
		this.message = message;
		this.issueTokenInfo = issueTokenInfo;
	}

	public static TokenResponse from(String message, IssueTokenInfo issueTokenInfo) {
		return new TokenResponse(message, issueTokenInfo);
	}
}
