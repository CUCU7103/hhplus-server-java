package kr.hhplus.be.server.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
class ApiControllerAdvice {

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		return ResponseEntity.status(500).body(new ErrorResponse("500", e.getMessage()));
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ErrorResponse> handleException(CustomException e) {
		return ResponseEntity.status(e.getCustomErrorCode().getHttpStatus())
			.body(new ErrorResponse(e.getCustomErrorCode().getCode(), e.getMessage()));

	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		return ResponseEntity.badRequest().body(new ErrorResponse("400", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		return ResponseEntity.badRequest().body(new ErrorResponse("400", ex.getMessage()));
	}
}
