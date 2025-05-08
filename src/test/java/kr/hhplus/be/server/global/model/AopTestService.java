package kr.hhplus.be.server.global.model;

import org.springframework.stereotype.Service;

import kr.hhplus.be.server.global.support.lock.model.LockType;
import kr.hhplus.be.server.global.support.lock.model.WithLock;

@Service
public class AopTestService {

	private int counter = 0;

	@WithLock(
		key = "testKey",           // 락 키: balance:charge:123 과 같이 생성
		type = LockType.REDIS_SIMPLE,       // Redis Spin Lock 사용
		expireMillis = 5500
	)
	public void failedAcquireLock() {
		counter++;
	}

	@WithLock(
		key = "testKey",
		type = LockType.REDIS_PUBSUB,
		expireMillis = 5500
	)
	public void methodThrowException() {
		counter++;
		throw new RuntimeException("예외발생");
	}

	@WithLock(
		key = "testKey",
		type = LockType.REDIS_SIMPLE,
		expireMillis = 10000
	)
	public void successAcquiredLock() throws InterruptedException {
		Thread.sleep(5000);
		counter++;
	}

}
