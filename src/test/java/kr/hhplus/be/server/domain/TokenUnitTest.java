package kr.hhplus.be.server.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

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

}
