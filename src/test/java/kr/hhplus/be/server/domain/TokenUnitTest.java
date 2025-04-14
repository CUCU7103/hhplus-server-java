package kr.hhplus.be.server.domain;

import static org.assertj.core.api.Assertions.*;

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
	void 토큰_대기상태에서_대기순위1_및_ACTIVE_조건_성공하면_ACTIVE_변경() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser, TokenStatus.WAITING, UUID.randomUUID().toString());
		int waitingRank = 1;
		long activeTokenCount = 50L; // MAX_ACTIVE(=1000) 미만

		// when
		token.checkAndActivate(waitingRank, activeTokenCount);

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
		;
		Token token = Token.createToken(dummyUser, TokenStatus.WAITING, UUID.randomUUID().toString());
		int waitingRank = 2; // 1이 아님
		long activeTokenCount = 50L;

		// when
		token.checkAndActivate(waitingRank, activeTokenCount);

		// then
		assertThat(token.getStatus()).isEqualTo(TokenStatus.WAITING);
	}

	/**
	 * 실패 케이스 2:
	 *  - 토큰 상태가 WAITING이 아닌 경우 (예: 이미 ACTIVE 상태) checkAndActivate 호출 시
	 *    CustomException(INVALID_STATUS)이 발생해야 한다.
	 */
	@Test
	void 토큰_대기상태_아닌경우_예외발생() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser, TokenStatus.ACTIVE, UUID.randomUUID().toString());
		int waitingRank = 1;
		long activeTokenCount = 50L;

		// when & then
		assertThatThrownBy(() -> token.checkAndActivate(waitingRank, activeTokenCount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_STATUS.getMessage());
	}

	/**
	 * 토큰 상태가 ACTIVE이고 생성 후 10분이 지난 경우,
	 * expireIfOlderThanTenMinutes() 호출 시 토큰 상태가 EXPIRED로 변경되어야 한다.
	 */
	@Test
	void 토큰_ACTIVE상태에서_생성후_10분초과하면_EXPIRED로_변경됨() {

		User dummyUser = User.builder().id(1L).build();
		Token token = Token.createToken(dummyUser, TokenStatus.ACTIVE, UUID.randomUUID().toString());

		// 현재 시간을 기준으로 11분 전으로 강제 설정하여 만료 조건 만족
		LocalDateTime now = LocalDateTime.now();
		ReflectionTestUtils.setField(token, "createdAt", now.minusMinutes(11));
		// expireIfOlderThanTenMinutes()는 expirationAt 필드를 사용하므로, 강제로 만료되도록 이전 시각 설정
		ReflectionTestUtils.setField(token, "expirationAt", now.minusMinutes(1));

		token.expireIfOlderThanTenMinutes();

		assertThat(token.getStatus()).isEqualTo(TokenStatus.EXPIRED);
	}

	/**
	 * 토큰 상태가 ACTIVE이지만 생성 후 10분이 지나지 않은 경우,
	 * expireIfOlderThanTenMinutes() 호출 시 상태가 그대로 ACTIVE로 유지되어야 한다.
	 */
	@Test
	void 토큰_ACTIVE상태에서_생성후_10분미만이면_상태유지됨() {

		User dummyUser = User.builder().id(1L).build();
		Token token = Token.createToken(dummyUser, TokenStatus.ACTIVE, UUID.randomUUID().toString());

		// 현재 시간을 기준으로 5분 전으로 설정하여 만료 조건 미달
		LocalDateTime now = LocalDateTime.now();
		ReflectionTestUtils.setField(token, "createdAt", now.minusMinutes(5));
		ReflectionTestUtils.setField(token, "expirationAt", now.plusMinutes(5));

		token.expireIfOlderThanTenMinutes();

		assertThat(token.getStatus()).isEqualTo(TokenStatus.ACTIVE);
	}

	/**
	 * checkTokenStatus 메서드 테스트:
	 * 토큰이 EXPIRED 상태일 때 TOKEN_EXPIRED 예외가 발생하는지 확인
	 */
	@Test
	void checkTokenStatus_토큰이_만료된_경우_TOKEN_EXPIRED_예외발생() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser, TokenStatus.EXPIRED, UUID.randomUUID().toString());

		// when & then
		assertThatThrownBy(() -> token.checkTokenStatus())
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("customErrorCode", CustomErrorCode.TOKEN_EXPIRED);
	}

	/**
	 * checkTokenStatus 메서드 테스트:
	 * 토큰이 ACTIVE가 아닌 상태(WAITING)일 때 INVALID_STATUS 예외가 발생하는지 확인
	 */
	@Test
	void checkTokenStatus_토큰이_ACTIVE가_아닌_경우_INVALID_STATUS_예외발생() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser, TokenStatus.WAITING, UUID.randomUUID().toString());

		// when & then
		assertThatThrownBy(() -> token.checkTokenStatus())
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("customErrorCode", CustomErrorCode.INVALID_STATUS);
	}

	/**
	 * checkTokenStatus 메서드 테스트:
	 * 토큰이 ACTIVE 상태일 때 예외가 발생하지 않는지 확인
	 */
	@Test
	void checkTokenStatus_토큰이_ACTIVE인_경우_예외발생하지_않음() {
		// given
		User dummyUser = User.builder()
			.id(1L)
			.build();
		Token token = Token.createToken(dummyUser, TokenStatus.ACTIVE, UUID.randomUUID().toString());

		// when & then
		assertThatCode(() -> token.checkTokenStatus())
			.doesNotThrowAnyException();
	}

}
