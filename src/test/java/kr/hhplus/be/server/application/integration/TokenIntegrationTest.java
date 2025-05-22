package kr.hhplus.be.server.application.integration;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.application.token.info.IssueTokenInfo;
import kr.hhplus.be.server.application.token.info.SearchTokenInfo;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.support.resolver.CurrentUserIdArgumentResolver;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class TokenIntegrationTest {

	@Autowired
	private TokenService tokenService;
	@Autowired
	private UserJpaRepository userJpaRepository;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private CurrentUserIdArgumentResolver currentUserIdArgumentResolver;
	@Autowired
	private StringRedisTemplate redisTemplate;

	@BeforeEach
	void setUp() {
		log.info("기존 데이터 초기화");
		redisTemplate.delete("concert:selloutTime");
	}

	@Test
	void 대기_토큰이_없는_사용자는_토큰_발급에_성공한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("김씨").build());
		// act
		IssueTokenInfo info = tokenService.issueToken(user.getId());
		// assert
		assertThat(info).isNotNull();
		assertThat(info.userId()).isEqualTo(user.getId());
	}

	@Test
	void 대기열_토큰_순위_조회에_성공한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("박씨").build());
		// act
		SearchTokenInfo info = tokenService.searchTokenRank(user.getId());
		// assert
		assertThat(info).isNotNull();
		assertThat(info.userId()).isEqualTo(user.getId());
		assertThat(info.rank()).isEqualTo(0);
	}

	@Test
	void 대기열에서_토큰_활성화에_성공한다() {
		// 1. given: 대기열에 토큰 2개 삽입
		String user1 = "1001";
		String user2 = "1002";
		redisTemplate.opsForZSet().add(Token.getWaitingQueueKey(), user1, 1);
		redisTemplate.opsForZSet().add(Token.getWaitingQueueKey(), user2, 2);

		// 2. when: activeToken() 실행
		tokenService.activeToken();

		// 3. then: 대기열에서 제거됐는지
		Set<String> waitingNow = redisTemplate.opsForZSet().range(Token.getWaitingQueueKey(), 0, -1);
		assertThat(waitingNow).doesNotContain(user1, user2);

		// 4. then: 입장열(SET)에 포함됐는지
		Boolean isMember1 = redisTemplate.opsForSet().isMember(Token.getActiveQueueKey(), user1);
		Boolean isMember2 = redisTemplate.opsForSet().isMember(Token.getActiveQueueKey(), user2);
		assertThat(isMember1).isTrue();
		assertThat(isMember2).isTrue();

		// 5. then: 개별 토큰 만료 값/TTL 확인
		String expireKey1 = Token.getActiveQueueSpecificKey(user1);
		String expireKey2 = Token.getActiveQueueSpecificKey(user2);
		Long ttl1 = redisTemplate.getExpire(expireKey1);
		Long ttl2 = redisTemplate.getExpire(expireKey2);

		log.info("info1 {} ", ttl1);
		log.info("info2 {} ", ttl2);

		// 10분(600초) 이내면 정상
		assertThat(ttl1).isGreaterThan(590); // 10초 정도 여유
		assertThat(ttl2).isGreaterThan(590);
	}

	@Test
	void 활성화_토큰_만료처리_진행() {
		// arrange
		String user1 = "1001";
		String user2 = "1002";
		redisTemplate.opsForSet().add(Token.getActiveQueueKey(), user1);
		redisTemplate.opsForSet().add(Token.getActiveQueueKey(), user2);

		redisTemplate.opsForValue().set(Token.getActiveQueueSpecificKey(user1), "dummy", Duration.ofMinutes(10));
		// act
		tokenService.cleanupExpiredActiveTokens();
		// assert
		Set<String> stillActive = redisTemplate.opsForSet().members(Token.getActiveQueueKey());
		assertThat(stillActive).contains(user1);
		assertThat(stillActive).doesNotContain(user2);
	}

}

