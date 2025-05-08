package kr.hhplus.be.server.global.redis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.redis.RedisPubSubLockStrategy;

@ExtendWith(MockitoExtension.class)
public class RedisPubSubLockUnitTest {

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RLock rLock;

	@InjectMocks
	private RedisPubSubLockStrategy pubSubLock;

	private LockContext context;

	@Test
	void lock_획득에_성공하면_true_반환한다() throws InterruptedException {
		// arrange
		String key = "key";
		long timeoutMillis = 1000L;
		long expireMillis = 2000L;
		LockContext context = LockContext.createPubSubLockContext(key, timeoutMillis, expireMillis);

		// Redisson 클라이언트가 락 객체를 반환하도록 설정
		given(redissonClient.getLock(key)).willReturn(rLock);

		// 락 획득 시도 시 true 반환하도록 설정
		given(rLock.tryLock(timeoutMillis, expireMillis, TimeUnit.MILLISECONDS)).willReturn(true);

		// act
		boolean acquired = pubSubLock.lock(context);
		// assert
		assertThat(acquired).isTrue();
		verify(redissonClient).getLock(key); // redissonClient.getLock 메서드 호출 검증
		verify(rLock).tryLock(timeoutMillis, expireMillis, TimeUnit.MILLISECONDS); // RLock.tryLock 메서드 호출 검증
	}

	@Test
	void lock_획득에_실패하면_false_반환한다() throws InterruptedException {
		// arrange
		String key = "key";
		long timeoutMillis = 1000L;
		long expireMillis = 50L;
		LockContext context = LockContext.createPubSubLockContext(key, timeoutMillis, expireMillis);
		given(redissonClient.getLock(key)).willReturn(rLock);
		given(rLock.tryLock(timeoutMillis, expireMillis, TimeUnit.MILLISECONDS)).willReturn(false);

		// act
		boolean acquired = pubSubLock.lock(context);

		// assert
		assertThat(acquired).isFalse();
		then(redissonClient).should().getLock(key);
		then(rLock).should().tryLock(timeoutMillis, expireMillis, TimeUnit.MILLISECONDS);
	}

	@Test
	void lock_획득_중_인터럽트_발생하면_false_반환한다() throws InterruptedException {
		// arrange
		String key = "key";
		LockContext context = LockContext.createPubSubLockContext(key, 1000L, 50L);

		given(redissonClient.getLock(key)).willReturn(rLock);
		given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willThrow(new InterruptedException());

		// act
		boolean acquired = pubSubLock.lock(context);

		// assert
		assertThat(acquired).isFalse();
		verify(redissonClient).getLock(key);
		verify(rLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
		// 인터럽트 상태 확인
		assertThat(Thread.currentThread().isInterrupted()).isTrue();
	}

	@Test
	void unlock_현재_스레드가_락을_보유한_경우_락을_해제한다() {
		// arrange
		String key = "key";
		LockContext context = LockContext.createPubSubLockContext(key, 1000L, 50L);

		given(redissonClient.getLock(key)).willReturn(rLock);
		given(rLock.isHeldByCurrentThread()).willReturn(true);

		// act
		pubSubLock.unlock(context);

		// assert
		verify(redissonClient).getLock(key);
		verify(rLock).isHeldByCurrentThread();
		verify(rLock).unlock();
	}

	@Test
	void unlock_현재_스레드가_락을_보유하지_않은_경우_락을_해제하지_않는다() {
		// arrange
		String key = "key";
		LockContext context = LockContext.createPubSubLockContext(key, 1000L, 50L);

		when(redissonClient.getLock(key)).thenReturn(rLock);
		when(rLock.isHeldByCurrentThread()).thenReturn(false);

		// act
		pubSubLock.unlock(context);

		// assert
		verify(redissonClient).getLock(key);
		verify(rLock).isHeldByCurrentThread();
		verify(rLock, never()).unlock(); // unlock 메서드가 호출되지 않았는지 검증
	}

}
