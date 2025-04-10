package kr.hhplus.be.server.global.error;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	CustomErrorCode customErrorCode;

	public CustomException(CustomErrorCode customErrorCode) {
		super(customErrorCode.getMessage());
		this.customErrorCode = customErrorCode;
	}

}
