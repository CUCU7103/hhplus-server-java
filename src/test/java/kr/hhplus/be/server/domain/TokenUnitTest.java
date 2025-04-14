package kr.hhplus.be.server.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public class TokenUnitTest {

	/**
	 * 성공 케이스:
	 *  - 토큰 상태가 WAITING이고,
	 *  - 대기순위(waitingRank)가 1이며,
	 *  - 현재 ACTIVE 토큰 수(activeTokenCount)가 MAX_ACTIVE 미만이면
	 *    토큰이 ACTIVE 상태로 변경되어야 한다.
	 */
	@Test
	void 토큰_생성에_성공한다() {
		User dummyUser = User.builder()
			.id(1L)
			.build();

		Token token = Token.createToken(dummyUser);

		assertThat(token.getCreatedAt()).isNotNull();
		assertThat(token.getStatus()).isEqualTo(TokenStatus.WAITING);
		assertThat(token.getUser()).isEqualTo(dummyUser);

		// UUID 문자열이 유효한지 검증
		assertDoesNotThrow(() -> UUID.fromString(token.getTokenValue()));
	}

	@Test
	void 토큰_대기상태에서_대기순위1_및_ACTIVE_조건_성공하면_ACTIVE_변경() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser);
		int waitingRank = 1;
		long activeTokenCount = 50L; // MAX_ACTIVE(=1000) 미만

		// when
		token.activateToken(waitingRank, activeTokenCount);

		// then
		assertThat(token.getStatus()).isEqualTo(TokenStatus.ACTIVE);
	}

	/**
	 * 실패 케이스 1:
	 *  - 토큰 상태가 WAITING이고,
	 *  - 대기순위가 1이 아니면 상태가 변경되지 않고 WAITING 상태를 유지해야 한다.
	 */
	@Test
	void 토큰_대기상태에서_대기순위1_아니면_상태유지() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();

		Token token = Token.createToken(dummyUser);
		int waitingRank = 2; // 1이 아님
		long activeTokenCount = 50L;

		// when
		token.activateToken(waitingRank, activeTokenCount);

		// then
		assertThat(token.getStatus()).isEqualTo(TokenStatus.WAITING);
	}

	@Test
	void 토큰_상태를_만료로_변경시키는데_성공한다() {
		User user = mock(User.class);
		Token token = Token.createToken(user);

		token.expiredToken();

		assertThat(token.getStatus()).isEqualTo(TokenStatus.EXPIRED);
	}

	@Test
	void 이미_만료된_토큰을_만료시키면_예외처리() {
		User user = mock(User.class);
		Token token = Token.createToken(user);
		token.expiredToken();

		assertThatThrownBy(token::expiredToken).isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.TOKEN_EXPIRED.getMessage());
	}

	/**
	 * 토큰 상태가 ACTIVE이고 생성 후 10분이 지난 경우,
	 * expireIfOlderThanTenMinutes() 호출 시 토큰 상태가 EXPIRED로 변경되어야 한다.
	 */
	@Test
	void 토큰_생성_후_만료시간을_초과하면_만료상태로_변경되어진다() {

		User dummyUser = User.builder().id(1L).build();
		Token token = Token.createToken(dummyUser);
		// 토큰 상태를 ACTIVE로 변경하여 조건 만족
		ReflectionTestUtils.setField(token, "status", TokenStatus.ACTIVE);
		// 현재 시간을 기준으로 11분 전으로 강제 설정하여 만료 조건 만족
		LocalDateTime now = LocalDateTime.now();
		// expireIfOlderThanTenMinutes()는 expirationAt 필드를 사용하므로, 강제로 만료되도록 이전 시각 설정
		ReflectionTestUtils.setField(token, "expirationAt", now.plusMinutes(12));

		token.expireTokenIfTimedOut();

		assertThat(token.getStatus()).isEqualTo(TokenStatus.EXPIRED);
	}

	/**
	 * 토큰 상태가 ACTIVE이지만 생성 후 10분이 지나지 않은 경우,
	 * expireIfOlderThanTenMinutes() 호출 시 상태가 그대로 ACTIVE로 유지되어야 한다.
	 */
	@Test
	void 만료시간이_초과하지_않았다면_토큰_활성화_상태를_유지한다() {

		User dummyUser = User.builder().id(1L).build();
		Token token = Token.createToken(dummyUser);
		ReflectionTestUtils.setField(token, "status", TokenStatus.ACTIVE);
		// 현재 시간을 기준으로 5분 전으로 설정하여 만료 조건 미달
		LocalDateTime now = LocalDateTime.now();
		ReflectionTestUtils.setField(token, "createdAt", now.minusMinutes(5));
		ReflectionTestUtils.setField(token, "expirationAt", now.plusMinutes(5));

		token.expireTokenIfTimedOut();

		assertThat(token.getStatus()).isEqualTo(TokenStatus.ACTIVE);
	}

	@Test
	void 최대_활성_토큰_갯수와_토큰의_대기순위가_1이라면_활성화에_성공한다() {
		User dummyUser = User.builder().id(1L).build();
		Token token = Token.createToken(dummyUser);
		token.activateToken(1, 980L);

		assertThat(token.getStatus()).isEqualTo(TokenStatus.ACTIVE);
	}

	@Test
	void 토큰의_상태가_대기가_아니라면_활성화에_실패한다() {
		User dummyUser = User.builder().id(1L).build();
		Token token = Token.createToken(dummyUser);
		ReflectionTestUtils.setField(token, "status", TokenStatus.ACTIVE);

		assertThatThrownBy(() -> token.activateToken(1, 980L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_STATUS.getMessage());
	}

	/**
	 * checkTokenStatus 메서드 테스트:
	 * 토큰이 EXPIRED 상태일 때 TOKEN_EXPIRED 예외가 발생하는지 확인
	 */
	@Test
	void 토큰상태_검증시_만료된_경우_TOKEN_EXPIRED_예외발생() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser);
		ReflectionTestUtils.setField(token, "status", TokenStatus.EXPIRED);
		// when & then
		assertThatThrownBy(token::validateTokenStatus)
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("customErrorCode", CustomErrorCode.TOKEN_EXPIRED);
	}

	/**
	 * checkTokenStatus 메서드 테스트:
	 * 토큰이 ACTIVE가 아닌 상태(WAITING)일 때 INVALID_STATUS 예외가 발생하는지 확인
	 */
	@Test
	void 토큰상태_검증시_ACTIVE가_아닌_경우_INVALID_STATUS_예외발생() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser);

		// when & then
		assertThatThrownBy(token::validateTokenStatus)
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("customErrorCode", CustomErrorCode.INVALID_STATUS);
	}
}
