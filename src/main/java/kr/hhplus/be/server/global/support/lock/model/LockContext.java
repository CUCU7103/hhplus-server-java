package kr.hhplus.be.server.global.support.lock.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LockContext {

	private final String key;  // 락을 식별하기 위한 고유 키
	private final long timeoutMillis; // 비즈니스 로직에서의 락 획득 최대 대기 시간 (밀리초)
	private final long retryIntervalMillis;  // 락 획득 재시도 간격 (밀리초)
	private final long expireMillis; // redis에서의 락 만료 시간

	private static final long DEFAULT_EXPIRE_MULTIPLIER = 2; // 기본적으로 타임아웃의 2배로 만료

	private LockContext(String key, long timeoutMills, long retryIntervalMillis, long expireMillis) {
		this.key = key;
		this.timeoutMillis = timeoutMills;
		this.retryIntervalMillis = retryIntervalMillis;
		this.expireMillis = expireMillis;
	}

	public static LockContext createSimpleLockContext(String key, long expireMillis) {
		return new LockContext(key, 0, 0, expireMillis);
	}

	public static LockContext createSpinLockContext(String key, long timeoutMillis, long retryIntervalMillis,
		long expireMillis) {
		if (expireMillis <= timeoutMillis) {
			throw new IllegalArgumentException("만료 시간(expireMillis)은 타임아웃 시간(timeoutMillis)보다 커야 합니다.");
		}
		return new LockContext(key, timeoutMillis, retryIntervalMillis, expireMillis);
	}

	public static LockContext createPubSubLockContext(String key, long timeoutMillis, long expireMillis) {
		if (expireMillis <= timeoutMillis) {
			throw new IllegalArgumentException("만료 시간(expireMillis)은 타임아웃 시간(timeoutMillis)보다 커야 합니다.");
		}
		return new LockContext(key, timeoutMillis, 0, expireMillis);
	}

}
