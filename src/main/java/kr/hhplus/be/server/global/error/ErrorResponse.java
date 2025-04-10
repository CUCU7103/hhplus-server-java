package kr.hhplus.be.server.global.error;

public record ErrorResponse(
	String code,
	String message
) {
}
