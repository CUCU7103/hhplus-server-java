package kr.hhplus.be.server.global.support.lock.redis;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.model.LockStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPubSubLockStrategy implements LockStrategy {

	private final RedissonClient redissonClient; // Redis 서버와 통신하는 Redisson 라이브러리의 클라이언트 객체

	@Override
	public boolean lock(LockContext context) { // 락 획득 시도 메서드, 성공 여부를 불리언으로 반환
		RLock lock = redissonClient.getLock(context.getKey());

		boolean acquired = false;
		try {
			acquired = lock.tryLock(
				context.getTimeoutMillis(),
				context.getExpireMillis(),
				TimeUnit.MILLISECONDS
			);
			if (acquired) {
				log.info("[락 획득 SUCCESS] key={} thread={}", context.getKey(), Thread.currentThread().getName());
			} else {
				log.info("[락 획득 FAILED ] key={} thread={}", context.getKey(), Thread.currentThread().getName());
			}
			return acquired;
		} catch (InterruptedException e) {
			log.info("[락 획득 INTERRUPTED] key={} thread={}", context.getKey(), Thread.currentThread().getName());
			Thread.currentThread().interrupt();
			return false;
		}
	}

	@Override
	public void unlock(LockContext context) { // 락 해제 메서드
		RLock lock = redissonClient.getLock(context.getKey()); // 락 획득 시 사용한 것과 동일한 키의 락 객체 참조
		if (lock.isHeldByCurrentThread()) { // 현재 스레드가 해당 락을 보유하고 있는지 확인 (안전 장치)
			lock.unlock(); // 락을 해제하고 Redis에 락 해제 이벤트 발행
		}
	}
}
