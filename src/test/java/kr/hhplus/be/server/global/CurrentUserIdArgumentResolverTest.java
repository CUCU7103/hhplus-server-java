package kr.hhplus.be.server.global;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.global.support.resolver.CurrentUserId;
import kr.hhplus.be.server.global.support.resolver.CurrentUserIdArgumentResolver;

public class CurrentUserIdArgumentResolverTest {
	private CurrentUserIdArgumentResolver resolver;

	@Mock
	private NativeWebRequest webRequest;

	@Mock
	private ModelAndViewContainer mavContainer;

	@Mock
	private WebDataBinderFactory binderFactory;

	// 테스트용 더미 MethodParameter 생성: dummyMethod에 @CurrentUserId 어노테이션이 적용된 파라미터 사용
	private MethodParameter createMethodParameter() throws Exception {
		return new MethodParameter(
			this.getClass().getDeclaredMethod("dummyMethod", long.class), 0);
	}

	// 더미 메서드 (테스트 목적으로 사용합니다.)
	public void dummyMethod(@CurrentUserId long userId) {
		// 내용은 상관없음
	}

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		resolver = new CurrentUserIdArgumentResolver();
	}

	@Test
	public void testSupportsParameter_withCurrentUserIdAnnotation() throws Exception {
		MethodParameter parameter = createMethodParameter();
		assertThat(resolver.supportsParameter(parameter)).isTrue();
	}

	@Test
	public void testValidUserId() throws Exception {
		// 정상 케이스: Header의 userId가 "123"인 경우, long 값 123L 반환
		given(webRequest.getHeader("userId")).willReturn("123");
		MethodParameter parameter = createMethodParameter();

		Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
		assertThat(result).isInstanceOf(Long.class)
			.isEqualTo(123L);
	}

	@Test
	public void testMissingUserIdHeader() throws Exception {
		// Header에 userId가 없는 경우 MissingRequestHeaderException 발생
		given(webRequest.getHeader("userId")).willReturn(null);
		MethodParameter parameter = createMethodParameter();

		assertThatThrownBy(() ->
			resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
			.isInstanceOf(MissingRequestHeaderException.class);
	}

	@Test
	public void testInvalidUserIdFormat() throws Exception {
		// 숫자 형식이 아닌 문자열 ("abc")가 전달되면 IllegalArgumentException 발생
		given(webRequest.getHeader("userId")).willReturn("abc");
		MethodParameter parameter = createMethodParameter();

		assertThatThrownBy(() ->
			resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("올바르지 않은 형식입니다");
	}

	@Test
	public void testUserIdLessThanOrEqualToZero() throws Exception {
		// "0"과 같이 부적절한 값이 전달되면 CustomException 발생
		when(webRequest.getHeader("userId")).thenReturn("0");
		MethodParameter parameter = createMethodParameter();

		assertThatThrownBy(() ->
			resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_USER_ID.getMessage());
	}

}
