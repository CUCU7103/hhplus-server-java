package kr.hhplus.be.server.application.token;

import static kr.hhplus.be.server.global.error.CustomErrorCode.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.token.info.IssueTokenInfo;
import kr.hhplus.be.server.application.token.info.SearchTokenInfo;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;

	@Transactional
	public IssueTokenInfo issueToken(long userId) {
		// 1) 유저 존재 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		// 2) Redis에서 요소가 있는지 확인하고 추가
		// 토큰을 먼저 생성
		// Sorted Set 스코어에 넣기 위해서 값 변환
		Token token = Token.create(userId);
		// 토큰이 없으면 추가, 있으면 유지
		tokenRepository.issueTokenNotExist(token);
		// 완성한 토큰 정보를 객체에 담아서 전달
		return IssueTokenInfo.from(token);
	}

	// 현재 자신이 몇번째 순위인지 조회
	@Transactional
	public SearchTokenInfo searchTokenRank(long userId) {
		// 유저 존재 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		// 현재 자신의 순위 조회하기
		long rank = tokenRepository.findUserRank(userId);
		if (rank < 0) {
			throw new CustomException(NOT_FOUND_TOKEN);
		}
		return SearchTokenInfo.from(Token.createIncludeRank(userId, rank));
	}

	// 스케줄러를 돌려서 주기적으로 상위 1000개의 토큰을 활성화

	/**
	 * SADD active-tokens 123 <br>
	 * – active-tokens라는 Set에 단순히 멤버 "123"을 추가합니다.<br>
	 * 별도 키 active-user-timestamp:123<br>
	 * – 이 키에 expireMillis 값을 저장하고, 10분 TTL을 설정합니다.<br>
	 * TTL 만료 시<br>
	 * – active-user-timestamp:123 키는 사라지지만,<br>
	 * – active-tokens Set의 멤버 "123"은 자동으로 제거되지 않습니다.<br>
	 * 따라서 “진짜 활성(키 살아 있음) 사용자” 목록을 유지하려면,<br>
	 * 스케줄러로 Set을 순회하면서 TTL 키(active-user-timestamp:123)가 없는 멤버를 SREM active-tokens 123으로 직접 지워줘야 합니다. <br>
	 * 이 구조 덕분에<br>
	 * 개별 만료 시각을 키 단위로 관리하면서도,<br>
	 * 활성 사용자 목록(Set) 은 일관성 있게 유지할 수 있습니다.
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	@Transactional
	public void activeToken() {
		// 대기열에서 상위 1000개 조회
		Set<String> topTokens = tokenRepository.top1000WaitingTokens();
		// 입장렬에 대기열 상위 1000개 토큰 저장 이때 입장렬의 score는 만료 처리를 진행하기 위해 현재시간 + 10분으로 지정
		// 2) 만료 시각 계산 및 TTL 정의
		long expireAtMillis = Instant.now()
			.plus(Duration.ofMinutes(10))
			.toEpochMilli();
		Duration ttl = Duration.ofMinutes(10);
		// 3) 하나씩 Set 추가 + 개별 키에 TTL 설정
		for (String userId : topTokens) {
			// (A) 입장열 Set에 추가 (SADD)
			tokenRepository.activeQueue(userId);
			// (B) 개별 키에 만료 타임스탬프 저장 + TTL
			tokenRepository.pushActiveQueue(userId, String.valueOf(expireAtMillis), ttl);
		}
		// 대기열에서 토큰제거
		tokenRepository.removeWaitingTokens(topTokens.toArray());
	}

	// 활성화 토큰이 만료 되었는지 확인하는 스케줄러
	@Scheduled(cron = "0 */1 * * * ?")  // 매 분 0초에 실행
	public void cleanupExpiredActiveTokens() {
		// 1) 현재 입장열에 남아 있는 모든 토큰 조회
		Set<String> activeTokens = tokenRepository.scanActiveQueue();
		if (activeTokens == null || activeTokens.isEmpty()) {
			return;
		}
		// 2) 순회하며 키 존재 여부 확인 → 없으면 Set 에서 제거
		for (String userId : activeTokens) {
			boolean stillActive = tokenRepository.hasKey(userId);
			if (!stillActive) {
				tokenRepository.removeActiveTokens(userId);
			}
		}
	}

}
