package kr.hhplus.be.server.global.error;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
class ApiControllerAdvice {

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
		log.error("Unhandled Exception occurred at [{} {}]", request.getMethod(), request.getRequestURI(), e);
		return ResponseEntity.status(500).body(new ErrorResponse("500", e.getMessage()));
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ErrorResponse> handleException(CustomException e, HttpServletRequest request) {
		log.warn("Handled CustomException at [{} {}] - code: {}, message: {}, location: {}:{}",
			request.getMethod(), request.getRequestURI(),
			e.getCustomErrorCode().getCode(), e.getMessage(),
			e.getStackTrace()[0].getFileName(),  // 파일명
			e.getStackTrace()[0].getLineNumber() // 라인 번호
		);
		return ResponseEntity.status(e.getCustomErrorCode().getHttpStatus())
			.body(new ErrorResponse(e.getCustomErrorCode().getCode(), e.getMessage()));

	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e,
		HttpServletRequest request) {
		String fieldErrors = e.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> String.format("[%s] %s", fieldError.getField(), fieldError.getDefaultMessage()))
			.collect(Collectors.joining(", "));
		log.warn("Validation failed at [{} {}] - {}", request.getMethod(), request.getRequestURI(), fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse("400", fieldErrors));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException e, HttpServletRequest request) {
		String fieldErrors = e.getFieldErrors().stream()
			.map(fieldError -> String.format("[%s] %s", fieldError.getField(), fieldError.getDefaultMessage()))
			.collect(Collectors.joining(", "));
		log.warn("Binding failed at [{} {}] - {}", request.getMethod(), request.getRequestURI(), fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse("400", fieldErrors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleJsonParse(HttpMessageNotReadableException e,
		HttpServletRequest request) {
		log.error("❌ JSON 파싱 실패 [{} {}] - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
		return ResponseEntity.status(400).body(new ErrorResponse("400", "잘못된 JSON 형식"));
	}

}
