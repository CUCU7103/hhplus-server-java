package kr.hhplus.be.server.global.support.lock.redis;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.model.LockStrategy;
import kr.hhplus.be.server.global.support.lock.model.WithLock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class RedisSimpleLockStrategy implements LockStrategy {

	private final StringRedisTemplate redisTemplate;
	private final Map<String, String> lockValues = new ConcurrentHashMap<>();

	// Lua 스크립트: 값이 일치할 때만 삭제
	private static final String UNLOCK_LUA =
		"if redis.call('get', KEYS[1]) == ARGV[1] then " +
			"return redis.call('del', KEYS[1]) else return 0 end ";

	// 1회 정의 후 재사용
	private static final DefaultRedisScript<Long> UNLOCK_SCRIPT =
		new DefaultRedisScript<>(UNLOCK_LUA, Long.class);

	//  해당 전략에 맞는 LockContext 생성 로직
	@Override
	public LockContext createContext(String key, WithLock lockAnnotation) {
		return LockContext.createSimpleLockContext(key, lockAnnotation.expireMillis());
	}

	@Override
	public boolean lock(LockContext context) {
		String key = context.getKey();
		String token = UUID.randomUUID().toString();

		log.info("Try lock. key={}, token={}", key, token);
		// 내부적으로 SET key <token> NX PX <expireMillis> 명령을 실행한다.
		Boolean success = redisTemplate.opsForValue()
			.setIfAbsent(key, token, context.getExpireMillis(), TimeUnit.MILLISECONDS);
		log.info("Insert key");
		if (Boolean.TRUE.equals(success)) {
			lockValues.put(key, token);
			return true;
		}
		return false;
	}

	@Override
	public void unlock(LockContext context) {
		String key = context.getKey();
		String token = lockValues.remove(key);
		if (token != null) {
			// Lua 스크립트를 통해 안전하게 키 삭제
			redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
		}
	}
}


