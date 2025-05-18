package kr.hhplus.be.server.global.support.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

	private final TokenService tokenService;

	@Override
	public boolean preHandle(HttpServletRequest request,
		HttpServletResponse response,
		Object handler) throws Exception {
		String userIdParam = request.getHeader("userId");
		if (userIdParam == null || userIdParam.isBlank()) {
			log.warn("[TokenInterceptor] userId header missing");
			throw new CustomException(CustomErrorCode.NOT_FOUND_HEADER);
		}
		long userId;
		try {
			userId = Long.parseLong(userIdParam);
		} catch (NumberFormatException e) {
			log.warn("[TokenInterceptor] invalid userId format: {}", userIdParam);
			throw new CustomException(CustomErrorCode.INVALID_USER_ID);
		}
		log.debug("[TokenInterceptor] validating token for userId={}", userId);
		// tokenService.validateTokenByUserId(userId);
		return true;
	}
}
