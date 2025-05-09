package kr.hhplus.be.server.global.redis;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.redis.RedisPubSubLockStrategy;

@SpringBootTest
@ActiveProfiles("test")
public class RedisPubSubLockIntegrationTest {

	@Autowired
	private RedisPubSubLockStrategy pubsubLock;

	@Autowired
	private RedissonClient redissonClient;

	// Redisson의 재진입 락(Reentrant Lock) 특성: Redisson의 RLock은 기본적으로 재진입 가능한 락(reentrant lock)입니다. 즉, 같은 스레드에서 이미 획득한 락을 다시 획득할 수 있습니다.
	private RLock rlock;

	private final String testLockKey = "key";

	@AfterEach
	void tearDown() {
		// 테스트 후 락 정리
		rlock = redissonClient.getLock(testLockKey);
		if (rlock.isLocked()) {
			try {
				rlock.forceUnlock();
			} catch (Exception e) {
				// 무시
			}
		}
	}

	@Test
	void 정상적으로_lock을_획득하고_lock을_반환한다() {

		LockContext lockContext = LockContext.createPubSubLockContext(testLockKey, 3000L, 5000L);
		// arrange
		boolean acquire = pubsubLock.lock(lockContext);
		assertThat(acquire).isTrue();

		rlock = redissonClient.getLock(testLockKey); // 락 가져와서 확인
		assertThat(rlock.isLocked()).isTrue();

		pubsubLock.unlock(lockContext); // 락 해제
		assertThat(rlock.isLocked()).isFalse(); // 락 가져와서 확인
		// assert
	}

	@Test
	void 이미_락이_점유된_상태에서_타임아웃_발생_시_락_획득에_실패한다() throws InterruptedException {
		// 첫 번째 락 획득 (긴 만료 시간 설정)
		LockContext firstLockContext = LockContext.createPubSubLockContext(testLockKey, 1000L, 10000L);
		boolean firstLockResult = pubsubLock.lock(firstLockContext);
		assertThat(firstLockResult).isTrue();

		// 동시성 테스트를 위한 래치
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean secondThreadResult = new AtomicBoolean(false);

		// 별도의 스레드에서 두 번째 락 획득 시도
		Thread secondThread = new Thread(() -> {
			try {
				// 두 번째 락 획득 시도 (짧은 타임아웃 설정)
				LockContext secondLockContext = LockContext.createPubSubLockContext(testLockKey, 500L, 5000L);
				boolean result = pubsubLock.lock(secondLockContext);
				secondThreadResult.set(result);

				// 만약 락을 획득했다면 해제
				if (result) {
					pubsubLock.unlock(secondLockContext);
				}
			} finally {
				latch.countDown();
			}
		});

		secondThread.start();
		latch.await(); // 두 번째 스레드가 완료될 때까지 대기

		// 두 번째 스레드의 락 획득 시도는 실패해야 함
		assertThat(secondThreadResult.get()).isFalse();

		// 정리
		pubsubLock.unlock(firstLockContext);
	}

	@Test
	void 락_만료_후에는_다른_스레드가_락을_획득할_수_있다() throws InterruptedException {
		LockContext firstLockContext = LockContext.createPubSubLockContext(testLockKey, 1000L, 1000L);
		boolean firstLockResult = pubsubLock.lock(firstLockContext);
		assertThat(firstLockResult).isTrue();

		// 락이 만료될 때까지 대기 (만료 시간보다 약간 더 기다림)
		Thread.sleep(1500L);

		// 다른 스레드에서 락 획득 시도
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean secondThreadResult = new AtomicBoolean(false);

		Thread secondThread = new Thread(() -> {
			try {
				// 다른 락 객체로 동일한 키에 대한 락 획득 시도
				LockContext newLockContext = LockContext.createPubSubLockContext(testLockKey, 1000L, 5000L);
				boolean result = pubsubLock.lock(newLockContext);
				secondThreadResult.set(result);

				// 만약 락을 획득했다면 해제
				if (result) {
					pubsubLock.unlock(newLockContext);
				}
			} finally {
				latch.countDown();
			}
		});

		secondThread.start();
		latch.await(); // 두 번째 스레드가 완료될 때까지 대기

		// 이전 락이 만료되었으므로 새 락 획득이 성공해야 함
		assertThat(secondThreadResult.get()).isTrue();
	}

	@Test
	void 락_만료시간이_지나면_자동으로_락이_만료되어지는지_확인() throws InterruptedException {
		LockContext lockContext = LockContext.createPubSubLockContext(testLockKey, 1000L, 1000L);
		boolean lockResult = pubsubLock.lock(lockContext);
		assertThat(lockResult).isTrue();

		//
		Thread.sleep(1500L);

		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean isLocked = new AtomicBoolean(true);

		Thread checkThread = new Thread(() -> {
			try {
				rlock = redissonClient.getLock(testLockKey);
				isLocked.set(rlock.isLocked());
			} finally {
				latch.countDown();
			}
		});

		checkThread.start();
		latch.await();

		assertThat(isLocked.get()).isFalse();

	}

	@Test
	void 락_획득_실패_후_재시도_성공_테스트() throws InterruptedException {
		// 첫 번째 락 획득 (짧은 만료 시간 설정)
		LockContext firstLockContext = LockContext.createPubSubLockContext(testLockKey, 1000L, 1000L);
		boolean firstLockResult = pubsubLock.lock(firstLockContext);
		assertThat(firstLockResult).isTrue();

		// 다른 스레드에서 락 획득 시도 및 재시도
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean finalResult = new AtomicBoolean(false);

		Thread retryThread = new Thread(() -> {
			try {
				// 첫 번째 시도 (짧은 타임아웃으로 실패해야 함)
				LockContext retryLockContext = LockContext.createPubSubLockContext(testLockKey, 500L, 5000L);
				boolean firstTry = pubsubLock.lock(retryLockContext);

				if (firstTry) {
					// 만약 성공했다면 해제하고 테스트 실패 (첫 번째 시도는 실패해야 함)
					pubsubLock.unlock(retryLockContext);
				} else {
					// 첫 번째 시도가 실패했으므로, 첫 번째 락이 만료될 때까지 대기
					Thread.sleep(1000);

					// 두 번째 시도 (첫 번째 락이 만료된 후이므로 성공해야 함)
					boolean secondTry = pubsubLock.lock(retryLockContext);
					finalResult.set(secondTry);

					if (secondTry) {
						pubsubLock.unlock(retryLockContext);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				latch.countDown();
			}
		});

		retryThread.start();
		latch.await(); // 재시도 스레드가 완료될 때까지 대기

		// 두 번째 시도는 성공해야 함
		assertThat(finalResult.get()).isTrue();
	}

}
