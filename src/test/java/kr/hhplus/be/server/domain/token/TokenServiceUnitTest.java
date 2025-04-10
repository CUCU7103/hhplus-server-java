package kr.hhplus.be.server.domain.token;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		TokenInfo tokenInfo = tokenService.issueToken(userId);

		// 결과 검증: 토큰 정보가 올바르게 생성되었는지 확인
		assertThat(tokenInfo).isNotNull();
		assertThat(tokenInfo.userId()).isEqualTo(userId);
		assertThat(tokenInfo.status()).isEqualTo(TokenStatus.WAITING);
	}

	@Test
	void 토큰_발급에_성공한다_기존토큰_사용() {
		long userId = 1L;
		User user = User.builder()
			.id(userId)
			.build();

		Token token = Token.createToken(user, TokenStatus.WAITING, UUID.randomUUID().toString());

		// 사용자가 존재하는 경우
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		// 기존 토큰이 있는 경우 해당 토큰을 반환해야 함
		given(tokenRepository.findByUserId(userId)).willReturn(Optional.of(token));

		TokenInfo tokenInfo = tokenService.issueToken(userId);

		// 결과 검증: 기존 토큰 정보와 동일한 정보를 반환하는지 확인
		assertThat(tokenInfo).isNotNull();
		assertThat(tokenInfo.userId()).isEqualTo(userId);
		assertThat(tokenInfo.status()).isEqualTo(TokenStatus.WAITING);
		assertThat(tokenInfo.tokenValue()).isEqualTo(token.getTokenValue());
	}

}
