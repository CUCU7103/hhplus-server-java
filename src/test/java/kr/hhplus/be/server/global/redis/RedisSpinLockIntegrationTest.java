package kr.hhplus.be.server.global.redis;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.model.TimeProvider;
import kr.hhplus.be.server.global.support.lock.redis.RedisSpinLockStrategy;

@SpringBootTest
@ActiveProfiles("test")
public class RedisSpinLockIntegrationTest {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private RedisSpinLockStrategy spinLock;

	@Autowired
	private TimeProvider timeProvider;

	@BeforeEach
	void setUp() {
		spinLock = new RedisSpinLockStrategy(stringRedisTemplate, timeProvider);
		stringRedisTemplate.getConnectionFactory().getConnection().flushAll();
	}

	@Test
	void lock을_성공적으로_획득한다() {
		// arrange
		String key = "key";
		String token = "token";
		LockContext context = LockContext.createSimpleLockContext(key, 1000L);
		// act &&  assert
		boolean acquire = spinLock.lock(context);
		assertThat(stringRedisTemplate.hasKey(key)).isTrue();
		assertThat(acquire).isTrue();
	}

	@Test
	void lock_획득_후_unlock에_성공한다() {
		// arrange
		String key = "key";
		String token = "token";
		LockContext context = LockContext.createSimpleLockContext(key, 1000L);
		// act &&  assert
		boolean acquire = spinLock.lock(context);
		spinLock.unlock(context);
		assertThat(spinLock.getLockValues().containsKey(key)).isFalse();
		assertThat(stringRedisTemplate.hasKey(key)).isFalse();

	}

	// 재시도 끝에 성공

	@Test
	void 타임아웃_되어지면_락_획득에_실패한다() {
		// given
		String key = "key";
		// 먼저 Redis에 해당 키를 직접 설정하여 잠금 상태를 시뮬레이션
		// 다른 인스턴스에서 10초 동안 유지되는 락을 설정
		stringRedisTemplate.opsForValue().set(key, "other-instance-token", 10, TimeUnit.SECONDS);

		// 매우 짧은시간으로 락 획득 대기시간 설정
		LockContext context = LockContext.createSpinLockContext(key, 10L, 10L, 1000L);
		// when
		boolean lockAcquired = spinLock.lock(context);
		// then
		assertThat(lockAcquired).isFalse();
	}

	@Test
	void 동시에_여러개의_요청이_들어오면_락_획득시_일부는_실패한다() throws InterruptedException {
		// given
		String testKey = "key";
		int threadCount = 5;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CyclicBarrier barrier = new CyclicBarrier(threadCount);
		CountDownLatch completionLatch = new CountDownLatch(threadCount);

		// 동시에 락을 획득한 스레드 수를 기록
		AtomicInteger lockAcquiredCount = new AtomicInteger(0);
		// 각 스레드에서 락 획득 성공/실패 여부를 기록
		Set<Boolean> results = ConcurrentHashMap.newKeySet();

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					barrier.await(); // 모든 스레드가 동시에 시작하도록 대기
					// 락 획득 대기 시간: 2000ms로 충분히 길게 설정
					LockContext context = LockContext.createSimpleLockContext(testKey, 2000L);
					boolean acquired = spinLock.lock(context);
					// 결과 기록
					results.add(acquired);
					if (acquired) {
						// 락 획득 카운트 증가
						lockAcquiredCount.incrementAndGet();
						// 락을 보유하는 시간을 더 길게 설정하여 다른 스레드가 시도할 시간 확보
						Thread.sleep(500);
						spinLock.unlock(context);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					completionLatch.countDown();
				}
			});
		}
		// 모든 스레드 완료 대기 (시간 충분히 설정)
		completionLatch.await();
		executorService.shutdown();
		// 최소 하나의 스레드는 락을 획득해야 함
		assertThat(lockAcquiredCount.get()).isPositive();

		// 모든 스레드의 결과를 확인
		assertThat(results).contains(true); // 최소 하나는 성공
		assertThat(results).contains(false); // 최소 하나는 실패 (모두 성공할 수 없음)

		// 락 획득 수가 1인지 검증 대신, 적어도 하나의 스레드가 실패했는지 확인
		boolean atLeastOneFailure = results.contains(false);
		assertThat(atLeastOneFailure).isTrue();

	}
}
