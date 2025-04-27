package kr.hhplus.be.server.global.support.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiFilter extends OncePerRequestFilter {
	// Jackson ObjectMapper 인스턴스 생성 - JSON 처리를 위한 객체
	private final ObjectMapper objectMapper = new ObjectMapper();

	// 필터 빈(Bean) 초기화 시 호출되는 메서드 오버라이드
	@Override
	protected void initFilterBean() throws ServletException {
		// 필터 초기화 시 로그 출력 (인스턴스 ID 함께 출력)
		log.info("ApiFilter 초기화 완료 - 인스턴스 ID: {}", System.identityHashCode(this));
		// 부모 클래스의 초기화 메서드 호출
		super.initFilterBean();
	}

	/**
	 * ContentCachingRequestWrapper와 ContentCachingResponseWrapper의 역할에 대한 설명 주석
	 * HTTP 요청과 응답의 바디는 일반적으로 한 번만 읽을 수 있는 스트림 형태임을 설명
	 * 서블릿 API의 특성상 요청 본문을 한 번만 읽을 수 있고, 재시도 시 예외 발생
	 *
	 * 이런 래퍼 클래스가 필요한 이유:
	 * 1. 여러 번 읽기: 필터와 컨트롤러에서 모두 본문을 읽을 수 있게 함
	 * 2. 비파괴적 읽기: 원본 요청/응답을 변경하지 않고 내용 검사 가능
	 */

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		long startTime = System.currentTimeMillis();

		try {
			log.info("REQUEST: {} {}", requestWrapper.getMethod(), requestWrapper.getRequestURI());
			logRequestHeaders(requestWrapper); // 헤더 추가 로그
			filterChain.doFilter(requestWrapper, responseWrapper);
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			int status = responseWrapper.getStatus();

			log.info("RESPONSE: {} ({}ms)", status, duration);

			logRequestBody(requestWrapper);
			logResponseBody(responseWrapper, requestWrapper.getMethod(), requestWrapper.getRequestURI(), status);

			responseWrapper.copyBodyToResponse();
		}
	}

	// 헤더 정보를 출력하기 한 메서드
	private void logRequestHeaders(ContentCachingRequestWrapper request) {
		Collections.list(request.getHeaderNames())
			.forEach(headerName -> Collections.list(request.getHeaders(headerName))
				.forEach(headerValue -> log.info("REQUEST HEADER: {}: {}", headerName, headerValue)));
	}

	// 요청 본문을 로깅하기 위한 메서드
	private void logRequestBody(ContentCachingRequestWrapper request) {
		byte[] content = request.getContentAsByteArray();
		if (content.length > 0) {
			String contentType = request.getContentType();
			String body = new String(content, StandardCharsets.UTF_8);
			logBody("REQUEST", contentType, body, false);
		}
	}

	// 응답 본문을 로깅하기 위한 메서드
	private void logResponseBody(ContentCachingResponseWrapper response, String method, String uri, int status) {
		byte[] content = response.getContentAsByteArray();
		if (content.length > 0) {
			String contentType = response.getContentType();
			String body = new String(content, StandardCharsets.UTF_8);
			logBody("RESPONSE", contentType, body, status >= 400);
		}
	}

	private void logBody(String label, String contentType, String body, boolean isError) {
		try {
			// Content-Type이 JSON 형식인지 확인 (예: application/json, application/vnd.api+json 등 포함 가능)
			if (contentType != null && contentType.contains("application/json")) {
				// Jackson의 ObjectMapper를 사용해 문자열을 JsonNode 트리 구조로 파싱 (유효한 JSON인지 검사)
				JsonNode jsonNode = objectMapper.readTree(body);
				// JsonNode를 예쁘게 포맷된(줄바꿈 포함) JSON 문자열로 변환
				String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
				// 에러 로그로 출력할지 여부에 따라 로그 레벨 결정
				if (isError) {
					// 에러 상황일 경우 error 레벨로 예쁘게 출력된 JSON 로그 기록
					log.error("{} BODY [{}]:\n{}", label, contentType, pretty);
				} else {
					// 정상 상황일 경우 info 레벨로 예쁘게 출력된 JSON 로그 기록
					log.info("{} BODY [{}]:\n{}", label, contentType, pretty);
				}
			} else {
				// JSON이 아닌 일반 텍스트 요청/응답 본문에 대한 로깅 처리
				if (isError) {
					// 에러 응답인 경우 error 레벨로 로그 출력
					log.error("{} BODY [{}]: {}", label, contentType, body);
				} else {
					// 정상 응답인 경우 info 레벨로 로그 출력
					log.info("{} BODY [{}]: {}", label, contentType, body);
				}
			}
		} catch (Exception e) {
			// JSON 파싱이 실패했을 경우 예외 발생 시 fallback 로깅 처리 (raw 문자열 그대로 출력)
			if (isError) {
				// 에러 상황일 경우 파싱되지 않은 원본 문자열을 error 레벨로 출력
				log.error("{} BODY [{}] (raw): {}", label, contentType, body);
			} else {
				// 정상 상황일 경우 파싱되지 않은 원본 문자열을 info 레벨로 출력
				log.info("{} BODY [{}] (raw): {}", label, contentType, body);
			}
		}
	}
}
