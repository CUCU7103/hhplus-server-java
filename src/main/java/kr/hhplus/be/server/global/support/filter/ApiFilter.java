package kr.hhplus.be.server.global.support.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiFilter extends OncePerRequestFilter {
	private final ObjectMapper objectMapper = new ObjectMapper();  // JSON 처리를 위한 ObjectMapper 인스턴스
	private final Tracer tracer;  // Micrometer Tracer 주입

	@Override  // 부모 클래스의 메서드를 오버라이드
	protected void initFilterBean() throws ServletException {  // 필터 빈 초기화 시 호출
		log.info("ApiFilter 초기화 완료 - 인스턴스 ID: {}", System.identityHashCode(this));  // 필터 초기화 로그 출력
		super.initFilterBean();  // 부모 클래스 초기화 로직 실행
	}

	@Override  // 부모 클래스의 메서드를 오버라이드
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);  // 요청 바디 캐싱 래퍼 생성
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);  // 응답 바디 캐싱 래퍼 생성

		long startTime = System.currentTimeMillis();  // 요청 시작 시간 기록

		try {
			injectTraceInfoToMDC();  // 현재 Tracing 정보를 MDC에 주입
			log.info("REQUEST: {} {}", requestWrapper.getMethod(), requestWrapper.getRequestURI());  // 요청 메서드 및 URI 로그
			logRequestHeaders(requestWrapper);  // 요청 헤더 전체 로그

			filterChain.doFilter(requestWrapper, responseWrapper);  // 다음 필터 또는 서블릿으로 요청/응답 전달

		} finally {
			long duration = System.currentTimeMillis() - startTime;  // 처리 시간 계산
			int status = responseWrapper.getStatus();  // 응답 상태 코드 가져오기

			log.info("RESPONSE: {} ({}ms)", status, duration);  // 응답 상태 및 처리 시간 로그

			logRequestBody(requestWrapper);  // 요청 바디 로그
			logResponseBody(responseWrapper, requestWrapper.getMethod(), requestWrapper.getRequestURI(),
				status);  // 응답 바디 로그

			responseWrapper.copyBodyToResponse();  // 래퍼된 응답 바디를 원래 객체로 복사
			MDC.clear();  // MDC 클리어하여 다음 요청에 영향 없도록 함
		}
	}

	private void injectTraceInfoToMDC() {  // MDC에 Trace 정보 주입하는 헬퍼 메서드
		Span currentSpan = tracer.currentSpan();  // 현재 Span 정보 가져오기
		if (currentSpan != null) {  // Span이 존재할 경우
			MDC.put("traceId", currentSpan.context().traceId());  // traceId를 MDC에 저장
			MDC.put("spanId", currentSpan.context().spanId());  // spanId를 MDC에 저장
		}
	}

	private void logRequestHeaders(ContentCachingRequestWrapper request) {  // 요청 헤더를 모두 로그 출력하는 메서드
		Collections.list(request.getHeaderNames())  // 헤더 이름 리스트 획득
			.forEach(headerName -> Collections.list(request.getHeaders(headerName))  // 각 헤더의 값 리스트 획득
				.forEach(headerValue -> log.info("REQUEST HEADER: {}: {}", headerName, headerValue)));  // 헤더 이름과 값 로그
	}

	private void logRequestBody(ContentCachingRequestWrapper request) {  // 요청 바디를 로그 출력하는 메서드
		byte[] content = request.getContentAsByteArray();  // 캐싱된 요청 바디 바이트 배열 획득
		if (content.length > 0) {  // 바디가 비어있지 않을 때
			String contentType = request.getContentType();  // 콘텐츠 타입 획득
			String body = new String(content, StandardCharsets.UTF_8);  // 바디를 문자열로 변환
			logBody("REQUEST", contentType, body, false);  // 로그 출력 (비에러)
		}
	}

	private void logResponseBody(ContentCachingResponseWrapper response, String method, String uri,
		int status) {  // 응답 바디 로그 메서드
		byte[] content = response.getContentAsByteArray();  // 캐싱된 응답 바디 바이트 배열 획득
		if (content.length > 0) {  // 바디가 비어있지 않을 때
			String contentType = response.getContentType();  // 콘텐츠 타입 획득
			String body = new String(content, StandardCharsets.UTF_8);  // 바디를 문자열로 변환
			logBody("RESPONSE", contentType, body, status >= 400);  // 상태 코드에 따라 에러 여부 결정
		}
	}

	private void logBody(String label, String contentType, String body, boolean isError) {  // JSON 포맷팅 및 로깅 메서드
		try {
			if (contentType != null && contentType.contains("application/json")) {  // JSON 타입 체크
				JsonNode jsonNode = objectMapper.readTree(body);  // JSON 파싱
				String pretty = objectMapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(jsonNode);  // pretty-print JSON
				if (isError) {
					log.error("{} BODY [{}]:\n{}", label, contentType, pretty);  // 에러 레벨로 로그
				} else {
					log.info("{} BODY [{}]:\n{}", label, contentType, pretty);  // 정보 레벨로 로그
				}
			} else {
				if (isError) {
					log.error("{} BODY [{}]: {}", label, contentType, body);  // 에러 레벨 일반 바디 로그
				} else {
					log.info("{} BODY [{}]: {}", label, contentType, body);  // 정보 레벨 일반 바디 로그
				}
			}
		} catch (Exception e) {  // 예외 발생 시 raw 바디 로그
			if (isError) {
				log.error("{} BODY [{}] (raw): {}", label, contentType, body);  // 에러 레벨로 raw 로그
			} else {
				log.info("{} BODY [{}] (raw): {}", label, contentType, body);  // 정보 레벨로 raw 로그
			}
		}
	}
}
