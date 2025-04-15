package kr.hhplus.be.server.global.support.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

	private final TokenService tokenService;

	@Override
	public boolean preHandle(HttpServletRequest request,
		HttpServletResponse response, Object handler) throws
		Exception {

		// 1) Request에서 userId 파라미터를 추출
		String userIdParam = request.getParameter("userId");
		if (userIdParam == null || userIdParam.isBlank()) {
			// 다이어그램: "400 Bad Request" 상황, 유저 아이디 누락
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유저 아이디가 전달되지 않았습니다.");
			return false;
		}

		long userId;
		try {
			userId = Long.parseLong(userIdParam);
		} catch (NumberFormatException e) {
			// 숫자로 변환되지 않으면 Bad Request 응답
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 유저 아이디입니다.");
			return false;
		}

		try {
			// 2) userId로 토큰 검증 요청 (TokenService 내부에서 DB 조회 후 조건별 분기 처리)
			tokenService.validateTokenByUserId(userId);
		} catch (CustomException e) {
			// 토큰이 없거나 유효하지 않을 경우 401 또는 400 응답
			switch (e.getCustomErrorCode()) {
				case NOT_FOUND_TOKEN:
				case INVALID_STATUS:
				case TOKEN_EXPIRED:
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
					break;
				default:
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
					break;
			}
			return false;
		}
		return true;
	}
}
