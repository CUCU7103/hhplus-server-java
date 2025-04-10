package kr.hhplus.be.server.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ErrorResponse> handleException(CustomException e) {
		return ResponseEntity.status(e.getCustomErrorCode().getHttpStatus())
			.body(new ErrorResponse(e.getCustomErrorCode().getCode(), e.getMessage()));

	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		return ResponseEntity.badRequest().body(new ErrorResponse("400", "파라미터 유효성 검증 실패"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		return ResponseEntity.badRequest().body(new ErrorResponse("400", "DTO 유효성 검증 실패"));
	}
}
