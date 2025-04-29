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
		return IssueTokenInfo.from(tokenRepository.findByUserIdAndWaitingToken(userId)
			.orElseGet(() -> tokenRepository.save(Token.createToken(user))));
	}

	@Transactional(readOnly = true)
	public SearchTokenInfo searchToken(long userId) {
		// 사용자 존재 여부 확인
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
		// 토큰 조회
		Token token = tokenRepository.findByUserIdAndWaitingToken(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_TOKEN));
		int waitingRank = token.getWaitingRank();
		return SearchTokenInfo.from(token, waitingRank);
	}

	@Transactional // 메서드 전체를 하나의 트랜잭션으로 묶어 DB 일관성 보장
	public ActiveTokenInfo activateToken(long tokenId) {
		// 해당 tokenId에 대해 쓰기 락(PESSIMISTIC_WRITE)을 획득하여 조회 (동시성 충돌 방지)
		Token token = tokenRepository.findTokenWithWriteLock(tokenId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_TOKEN)); // 없으면 예외 발생
		// 토큰이 만료시간이 지났으면 상태를 EXPIRED로 변경
		token.expireTokenIfTimedOut();
		// 현재 ACTIVE 상태의 토큰 수 조회 (동시 활성화 개수 제한 조건에 사용)
		long activeTokenCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
		// 사전에 생성 시 부여된 waitingRank 값을 가져옴 (AtomicInteger 기반 순차값)
		int waitingRank = token.getWaitingRank();
		// waitingRank가 0이고 ACTIVE 수가 제한 이하일 경우 상태를 ACTIVE로 전환
		token.activateToken(waitingRank, activeTokenCount);
		// 활성화 결과 정보를 반환
		return ActiveTokenInfo.from(token, waitingRank);
	}

	@Scheduled(fixedRate = 60 * 60 * 1000) // 1시간마다 실행
	@Transactional
	public void expireTokensScheduler() {
		List<Token> activeTokens = tokenRepository.findAllByStatus(TokenStatus.ACTIVE);
		LocalDateTime now = LocalDateTime.now();
		for (Token token : activeTokens) {
			// 생성시간 기준 10분이 지난 경우 EXPIRED로 전환
			if (LocalDateTime.now().isAfter(token.getExpirationAt())) {
				token.expiredToken();
			}
		}
	}

	@Transactional
	public void validateTokenByUserId(long userId) {
		Token token = tokenRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_TOKEN));
		// 만료 시간 검증 - 만료되었으면 상태 변경
		if (LocalDateTime.now().isAfter(token.getExpirationAt())) {
			token.expiredToken(); // 이 시점에 토큰 상태를 EXPIRED로 변경
			tokenRepository.save(token); // 변경된 상태 저장
			throw new CustomException(CustomErrorCode.TOKEN_EXPIRED);
		}
		token.validateTokenStatus();
	}

}
