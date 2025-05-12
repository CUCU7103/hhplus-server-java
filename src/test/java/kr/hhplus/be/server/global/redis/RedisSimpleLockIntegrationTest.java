package kr.hhplus.be.server.global.redis;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.redis.RedisSimpleLockStrategy;

@SpringBootTest
@ActiveProfiles("test")
public class RedisSimpleLockIntegrationTest {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private RedisSimpleLockStrategy lockStrategy;

	@BeforeEach
	void setUp() {
		lockStrategy = new RedisSimpleLockStrategy(stringRedisTemplate);
		// 테스트 시작 전 Redis에 남아있는 키를 제거
		stringRedisTemplate.getConnectionFactory().getConnection().flushAll();
	}

	@Test
	void 정상적으로_lock을_획득하고_lock을_반환한다() {
		// arrange
		String key = "getLock";
		long expire = 5000;
		LockContext context = LockContext.createSpinLockContext(key, 0, 0, expire);
		// act && assert
		boolean acquired = lockStrategy.lock(context);
		assertThat(acquired).isTrue();
		assertThat(stringRedisTemplate.hasKey(key)).isTrue();

		lockStrategy.unlock(context);
		assertThat(stringRedisTemplate.hasKey(key)).isFalse();
	}

	@Test
	void 같은_key에_중복으로_락을_획득할_수_없다() {
		// arrange
		String key = "getLock";
		long expire = 5000;
		LockContext context = LockContext.createSpinLockContext(key, 0, 0, expire);
		// act
		boolean acquired = lockStrategy.lock(context);
		boolean acquired2 = lockStrategy.lock(context);
		assertThat(acquired).isTrue();
		assertThat(acquired2).isFalse();
	}

	@Test
	void 여러_스레드가_동시에_접근하면_락_획득은_하나만_성공한다() throws InterruptedException {
		int threadCount = 10;
		String key = "getLock";
		long expire = 5000;
		LockContext context = LockContext.createSpinLockContext(key, 0, 0, expire);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		// CyclicBarrier로 대체 - 모든 스레드가 준비되면 동시에 시작
		CyclicBarrier barrier = new CyclicBarrier(threadCount);
		CountDownLatch completionLatch = new CountDownLatch(threadCount);

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					barrier.await(); // 모든 스레드가 이 지점에 도달할 때까지 대기한 후 동시에 실행
					if (lockStrategy.lock(context)) {
						successCount.incrementAndGet();
						lockStrategy.unlock(context);  // 락 해제
					} else {
						failCount.incrementAndGet();
					}
				} catch (Exception e) {
					failCount.incrementAndGet();
				} finally {
					completionLatch.countDown();
				}
			});
		}

		completionLatch.await(); // 모든 스레드가 작업을 완료할 때까지 대기
		executor.shutdown();

		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(threadCount - 1);
	}

}
