package kr.hhplus.be.server.global.redis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.redis.RedisSimpleLockStrategy;

@ExtendWith(MockitoExtension.class)
public class RedisSimpleLockUnitTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private ValueOperations<String, String> valueOps;

	@InjectMocks
	private RedisSimpleLockStrategy simpleLock;

	private LockContext context;

	@Test
	void lock_획득에_성공하면_true_반환하고_값을_저장한다() {
		// arrange
		String key = "testKey";
		long expire = 5000;
		LockContext ctx = LockContext.createSimpleLockContext(key, expire);
		// opsForValue() 호출 시 모킹한 valueOps 반환
		//RedisSimpleLockStrategy#lock(...) 내부에서 redisTemplate.opsForValue()를 호출하면, 실제로는 StringRedisTemplate이 미리 설정된 ValueOperations 인스턴스를 반환해 줘야 합니다.
		// 테스트 환경에서는 스프링 컨텍스트를 띄우지 않기 때문에, stringRedisTemplate.opsForValue()가 null을 반환하거나 예외를 던지지 않도록 “이 메서드 호출 시 우리가 미리 준비한 valueOps 목(mock) 객체를 돌려달라”는 의미죠
		given(stringRedisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.setIfAbsent(eq(key), anyString(), eq(expire), eq(TimeUnit.MILLISECONDS))).willReturn(true);

		// act
		boolean result = simpleLock.lock(ctx);

		// asssert
		assertThat(result).isTrue();
		then(valueOps).should().setIfAbsent(eq(key), anyString(), eq(expire), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	void lock_획득에_실패하면_false를_반환한다() {
		// arrange
		String key = "testKey";
		long expire = 5000;
		long timeoutMillis = 1000;
		long retryIntervalMillis = 2000;
		LockContext ctx = LockContext.createSimpleLockContext(key, expire);
		// opsForValue() 호출 시 모킹한 valueOps 반환
		//RedisSimpleLockStrategy#lock(...) 내부에서 redisTemplate.opsForValue()를 호출하면, 실제로는 StringRedisTemplate이 미리 설정된 ValueOperations 인스턴스를 반환해 줘야 합니다.
		// 테스트 환경에서는 스프링 컨텍스트를 띄우지 않기 때문에, stringRedisTemplate.opsForValue()가 null을 반환하거나 예외를 던지지 않도록 “이 메서드 호출 시 우리가 미리 준비한 valueOps 목(mock) 객체를 돌려달라”는 의미죠
		given(stringRedisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.setIfAbsent(eq(key), anyString(), eq(expire), eq(TimeUnit.MILLISECONDS))).willReturn(false);
		// act
		boolean result = simpleLock.lock(ctx);
		// assert
		assertThat(result).isFalse();
		// “Redis 스크립트 실행 메서드 execute(...)가 절대 호출되지 않아야 한다”를 검증하는 구문입니다.
		// DefaultRedisScript<T> 는 Spring Data Redis에서 제공하는, Lua 스크립트를 Redis에 안전하게 전달·실행하기 위한 스크립트 래퍼(wrapper) 클래스입니다.
		then(stringRedisTemplate).should(never()).execute(any(DefaultRedisScript.class), anyList(), any());

	}

	@Test
	void 획득한_lock을_해제한다() {
		// given
		String key = "testKey";
		long expire = 5000;
		LockContext ctx = LockContext.createSimpleLockContext(key, expire);
		String token = UUID.randomUUID().toString();

		// lockValues 맵에 미리 토큰 삽입
		Map<String, String> lockValuesMap = new ConcurrentHashMap<>();
		lockValuesMap.put(key, token);
		ReflectionTestUtils.setField(simpleLock, "lockValues", lockValuesMap);

		// when
		simpleLock.unlock(ctx);

		// then
		assertThat(simpleLock.getLockValues().containsKey(key)).isFalse();
		then(stringRedisTemplate).should().execute(any(DefaultRedisScript.class), anyList(), eq(token));
	}
}
