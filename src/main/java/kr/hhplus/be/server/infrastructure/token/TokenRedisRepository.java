package kr.hhplus.be.server.infrastructure.token;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.token.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TokenRedisRepository {
	private final StringRedisTemplate redisTemplate;

	// 토큰이 없으면 만들고 있으면 유지한다.
	public boolean issueTokenNotExist(Token token, String waitingKey) {
		// 값이 있으면 추가하지 않고 없으면 추가함.
		// 추가하면 true 실패하면 false
		return Boolean.TRUE.equals(
			redisTemplate.opsForZSet().add(waitingKey, token.getUserId(), token.getEpochSeconds()));
	}

	public long findUserRank(long userId, String waitingKey) {
		Long rank = redisTemplate.opsForZSet().rank(waitingKey, String.valueOf(userId));
		return rank != null ? rank : -1;
	}

	public Set<String> top1000WaitingTokens(String waitingKey) {
		return redisTemplate.opsForZSet().range(waitingKey, 0, 999);
	}

	public void activeQueue(String userId, String activeKey) {
		redisTemplate.opsForSet().add(activeKey, userId);
	}

	public void pushActiveQueue(String expireMillis, Duration ttl, String activeKey) {
		redisTemplate.opsForValue().set(activeKey, expireMillis, ttl);
	}

	public void removeWaitingTokens(Object topTokens, String waitingKey) {
		redisTemplate.opsForZSet().remove(waitingKey, (Object[])topTokens);
	}

	public Set<String> scanActiveQueue(String activeKey) {
		return redisTemplate.opsForSet().members(activeKey);
	}

	public boolean hasKey(String activeKey) {
		log.info(activeKey);
		return redisTemplate.hasKey(activeKey);
	}

	public void removeActiveTokens(String userId, String activeKey) {
		redisTemplate.opsForSet().remove(activeKey, userId);
	}

	public boolean existsInSortedSet(String userId, String waitingKey) {
		// 점수 조회
		Double score = redisTemplate.opsForZSet().score(waitingKey, userId);
		if (score != null) {
			return true;      // 멤버 존재
		}
		return false;
	}
}
