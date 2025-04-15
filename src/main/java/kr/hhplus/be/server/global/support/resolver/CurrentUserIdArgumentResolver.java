package kr.hhplus.be.server.global.support.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// @CurrentUserId 어노테이션이 붙어 있으면 지원
		return parameter.hasParameterAnnotation(CurrentUserId.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory) throws Exception {
		// Header에서 'userId' 값을 추출
		String userId = webRequest.getHeader("userId");
		if (userId == null || userId.isEmpty()) {
			// userId가 없을 경우 MissingRequestHeaderException 발생
			throw new MissingRequestHeaderException("userId", parameter);
		}
		try {
			// userId를 long 타입으로 변환
			long userIdLong = Long.parseLong(userId);
			// 0 이하 또는 음수인 경우 예외 발생 (유효하지 않은 값)
			if (userIdLong <= 0) {
				throw new CustomException(CustomErrorCode.INVALID_USER_ID);
			}
			return userIdLong;
		} catch (NumberFormatException ex) {
			// 숫자 형식이 올바르지 않은 경우 예외 처리
			throw new IllegalArgumentException("올바르지 않은 형식입니다", ex);
		}

	}
}
