package kr.hhplus.be.server.presentation.interceptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.global.support.interceptor.TokenInterceptor;

class TokenInterceptorUnitTest {

	@Mock
	private TokenService tokenService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@InjectMocks
	private TokenInterceptor tokenInterceptor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	/**
	 * 정상 케이스:
	 * - 요청 파라미터 userId가 올바르게 전달되고,
	 * - tokenService.validateTokenByUserId(…)에서 예외가 발생하지 않으면
	 *   인터셉터가 true를 반환하여 정상 컨트롤러 진입이 허용된다.
	 */
	@Test
	void 정상적인_유저아이디_전달시_인터셉터_통과() throws Exception {
		// given
		when(request.getParameter("userId")).thenReturn("123");
		// tokenService.validateTokenByUserId(123L)가 정상 동작한다고 가정

		// when
		boolean result = tokenInterceptor.preHandle(request, response, new Object());

		// then
		assertThat(result).isTrue();
		verify(response, never()).sendError(anyInt(), anyString());
	}

	/**
	 * 실패 케이스 1:
	 * - 요청 파라미터에 userId가 누락된 경우,
	 *   인터셉터가 400(Bad Request) 응답을 보내고 false를 반환한다.
	 */
	@Test
	void 유저아이디_누락시_400응답() throws Exception {
		// given
		when(request.getParameter("userId")).thenReturn(null);

		// when
		boolean result = tokenInterceptor.preHandle(request, response, new Object());

		// then
		assertThat(result).isFalse();
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "유저 아이디가 전달되지 않았습니다.");
	}

	/**
	 * 실패 케이스 2:
	 * - 요청 파라미터 userId가 숫자로 변환이 불가능한 경우,
	 *   인터셉터가 400 응답("유효하지 않은 유저 아이디입니다.")을 보내고 false를 반환한다.
	 */
	@Test
	void 잘못된형식의_유저아이디_전달시_400응답() throws Exception {
		// given
		when(request.getParameter("userId")).thenReturn("abc");

		// when
		boolean result = tokenInterceptor.preHandle(request, response, new Object());

		// then
		assertThat(result).isFalse();
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 유저 아이디입니다.");
	}

	/**
	 * 실패 케이스 3:
	 * - 유효한 userId가 전달되었으나, 토큰 검증 과정(tokenService.validateTokenByUserId)에서
	 *   CustomException(NOT_FOUND_TOKEN 등)이 발생하면,
	 *   인터셉터는 401(Unauthorized) 응답을 보내고 false를 반환한다.
	 */
	@Test
	void 토큰_검증_서비스에서_예외발생시_401응답() throws Exception {
		// given
		when(request.getParameter("userId")).thenReturn("123");
		doThrow(new CustomException(CustomErrorCode.NOT_FOUND_TOKEN))
			.when(tokenService).validateTokenByUserId(123L);

		// when
		boolean result = tokenInterceptor.preHandle(request, response, new Object());

		// then
		assertThat(result).isFalse();
		verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, CustomErrorCode.NOT_FOUND_TOKEN.getMessage());
	}

}
