package kr.hhplus.be.server.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenInfo;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.token.TokenSearchInfo;
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

	@Transactional(readOnly = true)
	public TokenSearchInfo searchToken(long userId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Token token = tokenRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_TOKEN));

		token.expireIfOlderThanTenMinutes();
		// 현재 ACTIVE 토큰 수 조회
		long activeTokenCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
		// 대기 순위를 조회 후 도메인 메서드에 외부 정보 함께 전달
		int waitingRank = tokenRepository.getWaitingRank(token.getId());
		token.checkAndActivate(waitingRank, activeTokenCount);

		return TokenSearchInfo.from(token);

	}

	@Scheduled(fixedRate = 60 * 60 * 1000) // 1시간마다 실행
	@Transactional
	public void expireTokensScheduler() {
		List<Token> activeTokens = tokenRepository.findAllByStatus(TokenStatus.ACTIVE);
		LocalDateTime now = LocalDateTime.now();
		for (Token token : activeTokens) {
			// 생성시간 기준 10분이 지난 경우 EXPIRED로 전환
			if (token.getExpirationAt().isBefore(now)) {
				token.updateStatus(TokenStatus.EXPIRED);
			}
		}
	}

}
