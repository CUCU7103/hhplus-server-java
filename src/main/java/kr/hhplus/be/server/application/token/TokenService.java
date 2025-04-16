package kr.hhplus.be.server.application.token;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.token.info.ActiveTokenInfo;
import kr.hhplus.be.server.application.token.info.IssueTokenInfo;
import kr.hhplus.be.server.application.token.info.SearchTokenInfo;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
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
	public IssueTokenInfo issueToken(long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
		Token issueToken = tokenRepository.findByUserId(userId)
			.orElseGet(() -> Token.createToken(user));
		return IssueTokenInfo.from(issueToken);

	}

	@Transactional(readOnly = true)
	public SearchTokenInfo searchToken(long userId) {
		// 사용자 존재 여부 확인
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
		// 토큰 조회
		Token token = tokenRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_TOKEN));
		return SearchTokenInfo.from(token);
	}

	@Transactional
	public ActiveTokenInfo activateToken(long tokenId) {
		Token token = tokenRepository.findToken(tokenId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_TOKEN));
		// 만료 검증: 스케줄러 보완용 만료 체크 로직
		token.expireTokenIfTimedOut();
		// 현재 ACTIVE 토큰 수 조회
		long activeTokenCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
		// 대기 순위 조회 후 토큰 활성화 처리
		int waitingRank = tokenRepository.getWaitingRank(token.getId());
		token.activateToken(waitingRank, activeTokenCount);
		return ActiveTokenInfo.from(token);
	}

	@Scheduled(fixedRate = 60 * 60 * 1000) // 1시간마다 실행
	@Transactional
	public void expireTokensScheduler() {
		List<Token> activeTokens = tokenRepository.findAllByStatus(TokenStatus.ACTIVE);
		LocalDateTime now = LocalDateTime.now();
		for (Token token : activeTokens) {
			// 생성시간 기준 10분이 지난 경우 EXPIRED로 전환
			if (token.getExpirationAt().isAfter(now)) {
				token.expiredToken();
			}
		}
	}

	public void validateTokenByUserId(long userId) {
		Token token = tokenRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_TOKEN));
		token.validateTokenStatus();
	}

}
