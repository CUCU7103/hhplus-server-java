package kr.hhplus.be.server.application.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.application.token.info.SearchTokenInfo;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
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

	@Captor
	private ArgumentCaptor<String[]> tokensCaptor;

	@Test
	void 사용자를_찾을_수_없어_토큰_발급에_실패() {
		long userId = 1L;

		assertThatThrownBy(() -> tokenService.issueToken(userId)).isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());
	}

	@Test
	void 토큰_생성에_성공한다() {
		User user = User.builder().id(1L).name("철수").build();

		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
		Token token = Token.create(user.getId());

		tokenService.issueToken(user.getId());

		assertThat(token.getUserId()).isEqualTo(user.getId());
		assertThat(token.getIssuedAt()).isNotNull();
		assertThat(token.getEpochSeconds()).isNotNull();
	}

	@Test
	void 순위_조회시_유저의_토큰이_없으면_예외가_발생한다() {

		User user = User.builder().id(1L).name("철수").build();

		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
		given(tokenRepository.findUserRank(user.getId())).willReturn((long)-1);

		assertThatThrownBy(() -> tokenService.searchTokenRank(user.getId())).isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_TOKEN.getMessage());
	}

	@Test
	void 순위_조회시_유저의_아이디와_토큰을_성공적으로_조회한다() {
		User user = User.builder().id(1L).name("철수").build();

		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
		given(tokenRepository.findUserRank(user.getId())).willReturn(Long.valueOf(1));

		SearchTokenInfo info = tokenService.searchTokenRank(user.getId());

		assertThat(info.userId()).isEqualTo(user.getId());
		assertThat(info.rank()).isEqualTo(1);
	}

	@Test
	void 대기열에서_상위_토큰을_활성화시키고_만료시간을_설정_후_대기열에서_제거() {
		Set<String> mockTokens = new HashSet<>();
		mockTokens.add("token1");
		mockTokens.add("token2");
		mockTokens.add("token3");
		// given
		given(tokenRepository.top1000WaitingTokens()).willReturn(mockTokens);

		// when
		tokenService.activeToken();

		// then
		verify(tokenRepository).top1000WaitingTokens();

		// 각 토큰에 대해 활성 큐에 추가되는지 검증
		for (String token : mockTokens) {
			verify(tokenRepository).activeQueue(token);

			ArgumentCaptor<String> expireTimeCaptor = ArgumentCaptor.forClass(String.class);
			verify(tokenRepository).pushActiveQueue(eq(token), expireTimeCaptor.capture(), eq(Duration.ofMinutes(10)));

			// 만료 시간값이 숫자 형태인지 검증
			assertThat(expireTimeCaptor.getValue())
				.matches(value -> value.matches("\\d+"), "만료 시간은 숫자 형태여야 합니다");
		}
		// 대기열에서 토큰이 제거되는지 검증
		verify(tokenRepository).removeWaitingTokens(tokensCaptor.capture());
	}

	@Test
	void 대기열이_비어있을_때_처리가_정상적으로_진행되는지_검증() {
		// given
		given(tokenRepository.top1000WaitingTokens()).willReturn(new HashSet<>());

		// when & then
		assertThatCode(() -> tokenService.activeToken())
			.doesNotThrowAnyException();

		// 빈 배열로 removeWaitingTokens가 호출되는지 확인
		verify(tokenRepository).removeWaitingTokens(tokensCaptor.capture());
		assertThat(tokensCaptor.getValue())
			.as("빈 대기열일 경우 빈 배열이 전달되어야 합니다")
			.isEmpty();
	}

	@Test
	void 만료된_활성화_토큰을_제거한다() {
		// arrange
		Set<String> mockTokens = new HashSet<>();
		mockTokens.add("token1");
		mockTokens.add("token2");
		mockTokens.add("token3");
		// act
		given(tokenRepository.scanActiveQueue()).willReturn(mockTokens);
		given(tokenRepository.hasKey("token1")).willReturn(false);
		given(tokenRepository.hasKey("token2")).willReturn(true);
		given(tokenRepository.hasKey("token3")).willReturn(false);

		tokenService.cleanupExpiredActiveTokens();

		verify(tokenRepository, times(1)).scanActiveQueue();
		verify(tokenRepository, times(1)).hasKey("token1");
		verify(tokenRepository, times(1)).hasKey("token2");
		verify(tokenRepository, times(1)).hasKey("token3");

		// removeActiveTokens는 t1, t3 에만 호출
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(tokenRepository, times(2)).removeActiveTokens(captor.capture());
		assertThat(captor.getAllValues())
			.containsExactlyInAnyOrder("token1", "token3");

		// t2에 대해서는 호출이 없어야 함
		verify(tokenRepository, never()).removeActiveTokens("token2");

	}

}



