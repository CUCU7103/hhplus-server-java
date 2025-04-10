package kr.hhplus.be.server.domain.token;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;

	@Transactional
	public TokenInfo issueToken(long userId) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Token issueToken = tokenRepository.findByUserId(userId).orElseGet(() -> {
			return Token.createToken(user, TokenStatus.WAITING, UUID.randomUUID().toString());
		});

		return TokenInfo.from(issueToken);

	}

}
