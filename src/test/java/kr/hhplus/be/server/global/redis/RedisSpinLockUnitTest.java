package kr.hhplus.be.server.global.redis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.model.TimeProvider;
import kr.hhplus.be.server.global.support.lock.redis.RedisSpinLockStrategy;

@ExtendWith(MockitoExtension.class)
public class RedisSpinLockUnitTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private TimeProvider timeProvider;

	@Mock
	private ValueOperations<String, String> valueOps;

	@InjectMocks
	private RedisSpinLockStrategy spinLock;

	private LockContext context;

	@BeforeEach
	void setUp() {
		// 단위 테스트 대상인 RedisSpinLockStrategy 인스턴스를 직접 생성하면서, 생성자 의존성(StringRedisTemplate)으로 Mock 객체를 주입
		spinLock = new RedisSpinLockStrategy(stringRedisTemplate, timeProvider);
	}

	@Test
	void lock_획득에_성공하면_true_반환하고_값을_저장한다() {
		// given
		LockContext ctx = LockContext.createSpinLockContext("key1", 1_000L, 50L, 500L);
		given(stringRedisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.setIfAbsent(
			eq("key1"),
			anyString(),
			eq(500L),
			eq(TimeUnit.MILLISECONDS)
		)).willReturn(true);
		// when
		boolean acquired = spinLock.lock(ctx);
		// then
		assertThat(acquired).isTrue();
		assertThat(spinLock.getLockValues()).containsKey("key1");
	}

	@Test
	void 재시도_후_lock_획득에_성공한다() {
		// arrange
		LockContext ctx = LockContext.createSpinLockContext("key2", 1000L, 50L, 500L);
		given(stringRedisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.setIfAbsent(
			eq("key2"),
			anyString(),
			eq(500L),
			eq(TimeUnit.MILLISECONDS)
		)).willReturn(false, true);
		// when
		boolean acquired = spinLock.lock(ctx);
		// then
		assertThat(acquired).isTrue();
		then(valueOps).should(times(2))
			.setIfAbsent(
				eq("key2"),
				anyString(),
				eq(500L),
				eq(TimeUnit.MILLISECONDS)
			);

	}

	@Test
	void 타임아웃되어_락_획득에_실패한다() {
		// arrange
		LockContext ctx = LockContext.createSpinLockContext("key3", 1000L, 50L, 2000L);
		given(timeProvider.currentTime()).willReturn(1000L, 1500L, 2001L); // while 루프가 한 번 실행되어지고 종료
		given(stringRedisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.setIfAbsent(
			eq("key3"),
			anyString(),
			anyLong(),
			eq(TimeUnit.MILLISECONDS)
		)).willReturn(false);
		// act
		boolean acquired = spinLock.lock(ctx);
		// assert
		assertThat(acquired).isFalse();
		then(valueOps).should(times(1)).setIfAbsent(
			eq("key3"),
			anyString(),
			eq(2000L),
			eq(TimeUnit.MILLISECONDS));
	}

	@Test
	void unlock_성공하면_스크립트_호출후_맵에서제거된다() {
		// given
		String key = "key", token = "tok";
		spinLock.getLockValues().put(key, token);
		given(stringRedisTemplate.execute(
			any(DefaultRedisScript.class),
			eq(Collections.singletonList(key)),
			eq(token)
		)).willReturn(1L);

		// when
		spinLock.unlock(LockContext.createSimpleLockContext(key, 0));

		// then
		then(stringRedisTemplate).should().execute(
			any(DefaultRedisScript.class),
			eq(Collections.singletonList(key)),
			eq(token)
		);
		assertThat(spinLock.getLockValues()).doesNotContainKey(key);
	}

	@Test
	void unlock_토큰없으면_execute호출안됨() {
		// when
		spinLock.unlock(LockContext.createSimpleLockContext("unknown", 0));
		// then
		then(stringRedisTemplate).should(never())
			.execute(any(), anyList(), any());
	}

	@Test
	void lock_재시도중_인터럽트되면_false반환() {
		// given
		LockContext ctx = LockContext.createSimpleLockContext("key4", 1000L);
		given(stringRedisTemplate.opsForValue()).willReturn(valueOps);
		given(valueOps.setIfAbsent(any(), any(), anyLong(), any()))
			.willReturn(false);
		// 인터럽트 플래그 설정
		Thread.currentThread().interrupt();
		// when
		boolean acquired = spinLock.lock(ctx);

		// then
		assertThat(acquired).isFalse();
		// 이후 테스트에 영향 없도록 플래그 클리어
		Thread.interrupted();
	}

	@Test
	void unlock_중_예외_발생해도_토큰은_무조건_제거된다() {
		// given
		String key = "errKey", token = "tokErr";
		spinLock.getLockValues().put(key, token);
		given(stringRedisTemplate.execute(any(), anyList(), eq(token)))
			.willThrow(new RuntimeException("boom!!!"));

		// when
		assertThatCode(() -> spinLock.unlock(LockContext.createSimpleLockContext(key, 0)))
			.doesNotThrowAnyException();

		// then
		assertThat(spinLock.getLockValues()).doesNotContainKey(key);
	}

}
