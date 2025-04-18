package kr.hhplus.be.server.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.application.token.info.ActiveTokenInfo;
import kr.hhplus.be.server.application.token.info.IssueTokenInfo;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

@ExtendWith(MockitoExtension.class)
class TokenServiceUnitTest {

	@Mock
	private TokenRepository tokenRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private TokenService tokenService;

	@Test
	void 사용자를_찾을_수_없어_토큰_발급에_실패() {
		long userId = 1L;

		assertThatThrownBy(() -> tokenService.issueToken(userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());
	}

	@Test
	void 토큰_발급에_성공한다_신규발급() {
		long userId = 1L;
		User user = User.builder()
			.id(userId)
			.build();

		// 사용자가 존재하는 경우
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		// 기존 토큰이 없는 경우 신규 토큰 발급 로직으로 진입해야 함
		given(tokenRepository.findByUserId(userId)).willReturn(Optional.empty());

		//act
		IssueTokenInfo issueTokenInfo = tokenService.issueToken(userId);

		// 결과 검증: 토큰 정보가 올바르게 생성되었는지 확인
		assertThat(issueTokenInfo).isNotNull();
		assertThat(issueTokenInfo.userId()).isEqualTo(userId);
		assertThat(issueTokenInfo.status()).isEqualTo(TokenStatus.WAITING);
	}

	@Test
	void 토큰_발급에_성공한다_기존토큰_사용() {
		long userId = 1L;
		User user = User.builder()
			.id(userId)
			.build();

		Token token = Token.createToken(user);

		// 사용자가 존재하는 경우
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		// 기존 토큰이 있는 경우 해당 토큰을 반환해야 함
		given(tokenRepository.findByUserId(userId)).willReturn(Optional.of(token));

		IssueTokenInfo issueTokenInfo = tokenService.issueToken(userId);

		// 결과 검증: 기존 토큰 정보와 동일한 정보를 반환하는지 확인
		assertThat(issueTokenInfo).isNotNull();
		assertThat(issueTokenInfo.userId()).isEqualTo(userId);
		assertThat(issueTokenInfo.status()).isEqualTo(TokenStatus.WAITING);
		assertThat(issueTokenInfo.tokenValue()).isEqualTo(token.getTokenValue());
	}

	@Test
	void 토큰을_찾을_수_없어_토큰_대기번호_조회_실패() {
		long userId = 1L;
		User user = mock(User.class);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> tokenService.searchToken(userId)).isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_TOKEN.getMessage());

	}

	@Test
	void 토큰_활성화에_성공한다() {
		// given
		User user = mock(User.class);
		Token token = Token.createToken(user);

		given(tokenRepository.findToken(token.getId())).willReturn(Optional.of(token));
		given(tokenRepository.countByStatus(TokenStatus.ACTIVE)).willReturn(3L); // ACTIVE 수가 3개
		given(tokenRepository.getWaitingRank(token.getId())).willReturn(1); // 대기순위 1위

		// when
		ActiveTokenInfo result = tokenService.activateToken(token.getId());

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(TokenStatus.ACTIVE); // ACTIVE로 바뀌어야 함
	}

	@Test
	void 사용자에_해당하는_토큰_정보가_없어_조회에_실패() {
		// given
		long userId = 1L;
		User user = User.builder().id(userId).build();

		// 상태가 이미 ACTIVE인 토큰
		Token token = Token.createToken(user);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		// when & then
		assertThatThrownBy(() -> tokenService.searchToken(userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_TOKEN.getMessage());
	}

	@Test
	void 유효시간이_지난_토큰은_EXPIRED로_업데이트된다() {
		long userId = 1L;
		LocalDateTime now = LocalDateTime.now();
		User user = User.builder().id(userId).build();
		Token token1 = Token.createToken(user);
		ReflectionTestUtils.setField(token1, "expirationAt", now.plusMinutes(14));

		// 유효시간이 지났지만 만료되지 않은 토큰이 존재시
		Token token2 = Token.createToken(user);
		ReflectionTestUtils.setField(token2, "expirationAt", now.plusMinutes(12));
		ReflectionTestUtils.setField(token2, "status", TokenStatus.WAITING);

		// ACTIVE 상태 토큰 전체 리스트에 두 토큰을 포함시킴
		List<Token> activeTokens = Arrays.asList(token1, token2);

		// 토큰 저장소의 findAllByStatus 메서드가 ACTIVE 토큰 목록을 반환하도록 목 처리
		given(tokenRepository.findAllByStatus(TokenStatus.ACTIVE)).willReturn(activeTokens);

		// when : 스케줄러 메서드 호출 (실제 환경에서는 1시간마다 호출되지만, 테스트에서는 직접 호출)
		tokenService.expireTokensScheduler();

		// then : 토큰1은 EXPIRED 상태로 변경, 토큰2는 ACTIVE 상태 유지됨
		assertThat(token1.getStatus()).isEqualTo(TokenStatus.EXPIRED);
		assertThat(token2.getStatus()).isEqualTo(TokenStatus.EXPIRED);
	}
}
