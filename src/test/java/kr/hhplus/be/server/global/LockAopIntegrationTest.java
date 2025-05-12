package kr.hhplus.be.server.global;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.global.model.AopTestService;
import kr.hhplus.be.server.global.support.lock.redis.RedisSimpleLockStrategy;

@SpringBootTest
@ActiveProfiles("test")
public class LockAopIntegrationTest {

	@Autowired
	private AopTestService aopTestService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private RedissonClient redissonClient;

	private RLock rLock;

	@Autowired
	private RedisSimpleLockStrategy redisSimpleLockStrategy;

	@BeforeEach
	void tearDown() {
		redisTemplate.delete("testKey");
	}

	@Test
	void 락_획득에_실패하면_예외를_발생시킨다() {
		// 수동으로 락 획득
		String lockKey = "testKey";
		redisTemplate.opsForValue().set(lockKey, "locked", 5000, TimeUnit.MILLISECONDS);

		// 같은 키로 서비스 호출 시 예외 발생 확인
		assertThatThrownBy(() -> aopTestService.failedAcquireLock())
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.FAILED_ACQUIRE_LOCK.getMessage());

	}

	@Test
	void simple_spin_락_비즈니스_로직_수행중_예외가_발생하면_락을_해제한다() {
		// act
		assertThatThrownBy(() -> aopTestService.methodThrowException()).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("예외발생");
		assertThat(redisTemplate.hasKey("testKey")).isFalse(); // 언락 수행 확인
		// assert
	}

	@Test
	void pub_sub_락_비즈니스_로직_수행중_예외가_발생하면_락을_해제한다() throws InterruptedException {
		// act
		rLock = redissonClient.getLock("testKey");
		assertThatThrownBy(() -> aopTestService.methodThrowException()).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("예외발생");
		// 락 해제 후 다시 획득하기

		// 2) 락 해제 확인: isLocked()가 false 여야 함
		RLock lock = redissonClient.getLock("testKey");
		assertThat(lock.isLocked()).isFalse();
	}

	@Test
	void 로직_수행전_락을_획득하고_수행후_락을_해지한다() throws InterruptedException {
		// when
		Thread thread = new Thread(() -> {
			try {
				aopTestService.successAcquiredLock();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
		thread.start();
		Thread.sleep(1000);
		// then: 실행 중에는 락이 존재해야 함
		assertThat(redisTemplate.hasKey("testKey")).isTrue();
		thread.join(); // 메서드 종료까지 대기
		// then: 로직 종료 후 락은 해제되어야 함
		assertThat(redisTemplate.hasKey("testKey")).isFalse();
	}

}
