package kr.hhplus.be.server.global.support.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

	private final TokenService tokenService;

	@Override
	public boolean preHandle(HttpServletRequest request,
		HttpServletResponse response, Object handler) throws Exception {

		// 1) Request에서 userId 파라미터를 추출
		String userIdParam = request.getHeader("userId");
		if (userIdParam == null || userIdParam.isBlank()) {
			// 유저 아이디 누락 → 예외로 전환
			throw new CustomException(CustomErrorCode.INVALID_USER_ID);
		}
		long userId;
		try {
			userId = Long.parseLong(userIdParam);
		} catch (NumberFormatException e) {
			// 숫자로 변환 실패 → 예외로 전환
			throw new CustomException(CustomErrorCode.INVALID_USER_ID);
		}
		// 2) 토큰 검증 → 내부에서 CustomException 발생 가능
		tokenService.validateTokenByUserId(userId);

		return true;
	}
}
