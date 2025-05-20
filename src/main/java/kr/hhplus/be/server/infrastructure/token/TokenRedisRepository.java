package kr.hhplus.be.server.infrastructure.token;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.token.Token;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {
	private final StringRedisTemplate redisTemplate;
	private final String WAITING_KEY = "waiting:tokens";
	private final String ACTIVE_KEY = "active:tokens";

	// 토큰이 없으면 만들고 있으면 유지한다.
	public boolean issueTokenNotExist(Token token) {
		// 값이 있으면 추가하지 않고 없으면 추가함.
		// 추가하면 true 실패하면 false
		return Boolean.TRUE.equals(
			redisTemplate.opsForZSet().add(WAITING_KEY, token.getUserId(), token.getEpochSeconds()));
	}

	public long findUserRank(long userId) {
		Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, String.valueOf(userId));
		return rank != null ? rank : -1;
	}

	public Set<String> top1000WaitingTokens() {
		return redisTemplate.opsForZSet().range(WAITING_KEY, 0, 999);
	}

	public void activeQueue(String userId) {
		redisTemplate.opsForSet().add(ACTIVE_KEY, userId);
	}

	public void pushActiveQueue(String userId, String expireMillis, Duration ttl) {
		redisTemplate.opsForValue().set(ACTIVE_KEY + ":" + userId, expireMillis, ttl);
	}

	public void removeWaitingTokens(Object topTokens) {
		redisTemplate.opsForZSet().remove(WAITING_KEY, topTokens);
	}

	public Set<String> scanActiveQueue() {
		return redisTemplate.opsForSet().members(ACTIVE_KEY);
	}

	public boolean hasKey(String userId) {
		return redisTemplate.hasKey(ACTIVE_KEY + ":" + userId);
	}

	public void removeActiveTokens(String userId) {
		redisTemplate.opsForSet().remove(ACTIVE_KEY, userId);
	}

	public boolean existsInSortedSet(String userId) {
		// 점수 조회
		Double score = redisTemplate.opsForZSet().score(WAITING_KEY, userId);
		if (score != null) {
			return true;      // 멤버 존재
		}
		return false;
	}
}
