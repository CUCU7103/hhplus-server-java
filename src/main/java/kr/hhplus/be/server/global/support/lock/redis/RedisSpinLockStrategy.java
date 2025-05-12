package kr.hhplus.be.server.global.support.lock.redis;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.global.support.lock.model.LockContext;
import kr.hhplus.be.server.global.support.lock.model.LockStrategy;
import kr.hhplus.be.server.global.support.lock.model.TimeProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Spin Lock은 분산 환경에서 짧은 시간 동안 리소스에 대한 상호 배제를 보장하기 위해 사용되는 기법입니다. <br>
 * 클라이언트는 “이 리소스를 내게 할당해줘”라는 의미로 Redis에 특정 키를 설정(setNX)하고, 성공할 때까지 반복(스핀)하면서 시도합니다. <br>
 * (성공할때까지 시도하는데 비관적 락과 유사하다)<br>
 * 락을 획득하면 비즈니스 로직(크리티컬 섹션)을 실행한 뒤, 해당 키를 삭제(delete)하여 락을 해제합니다.
 *  즉 레디스틑 sigleThrea로 구성되있어서 여러 다중 인스턴스에서 요청이 들어와도 순차적으로 처리한다
 *  이때 spin lock 방식이라면 먼저 들어온 클라이언트가 락을 획득 요청을 진행한다. 이때 획득한다면 획득한 락을 가지고 비즈니스 로직을 수행한다.
 *  획득하지 못한 경우라면 락 만료 전 까지 지속적으로 재시도 요청으로 락 획득을 요청하고 획득 하면 비즈니스 로직 수행 아니라면 락 획득 실패 예외 처리를 진행한다.
 */

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class RedisSpinLockStrategy implements LockStrategy {

	private final StringRedisTemplate stringRedisTemplate;
	private final TimeProvider timeProvider;

	/**
	 * -- KEYS[1]: 해제하려는 락의 키 <br>
	 * -- ARGV[1]: 락 획득 시 저장해 둔 고유 토큰<br>
	 *<br>
	 * if redis.call('get', KEYS[1]) == ARGV[1] then <br>
	 *     -- 1) 현재 Redis에 저장된 값(GET)이 우리가 락을 걸 때 사용한 토큰(ARGV[1])과 같다면
	 * <br>
	 *     return redis.call('del', KEYS[1]) <br>
	 *     -- 2) KEYS[1] 키를 삭제(DEL)하고, 삭제된 키 수(보통 1)를 반환
	 *<br>
	 * else <br>
	 *     return 0 <br>
	 *     -- 3) 값이 다르다면(다른 프로세스가 이미 획득한 락) 아무것도 지우지 않고 0을 반환 <br>
	 * end
	 **/
	private static final String SCRIPT =
		"if redis.call('get', KEYS[1]) == ARGV[1] then " +
			"return redis.call('del', KEYS[1]) else return 0 end";

	private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(SCRIPT, Long.class);

	// 키별로 생성된 고유 토큰을 저장하여 해제 시 검증에 사용
	private final Map<String, String> lockValues = new ConcurrentHashMap<>();

	@Override
	public boolean lock(LockContext context) {
		String key = context.getKey();
		String token = UUID.randomUUID().toString();

		// 락 획득 시도 최대 대기 시간
		long deadline = timeProvider.currentTime() + context.getTimeoutMillis();

		while (true) {
			if (timeProvider.currentTime() >= deadline) {
				return false;  // 타임아웃 후 락 획득 실패
			}

			Boolean acquired = stringRedisTemplate.opsForValue()
				.setIfAbsent(key, token, context.getExpireMillis(), TimeUnit.MILLISECONDS);

			if (Boolean.TRUE.equals(acquired)) {
				log.info("[락 획득 SUCCESS] key={} thread={}", context.getKey(), Thread.currentThread().getName());
				lockValues.put(key, token);
				return true;
			}
			// thundering-herd 완화용 jitter 포함 재시도
			try {
				log.info("[락 획득 FAILED, 재시도 진행] key={} thread={}", context.getKey(), Thread.currentThread().getName());
				long jitter = ThreadLocalRandom.current().nextLong(context.getRetryIntervalMillis());
				Thread.sleep(context.getRetryIntervalMillis() + jitter);
			} catch (InterruptedException ie) {
				log.info("[락 획득 INTERRUPTED] key={} thread={}", context.getKey(), Thread.currentThread().getName());
				Thread.currentThread().interrupt();  // 인터럽트 상태 복원
				return false;
			}
		}
	}

	@Override
	public void unlock(LockContext context) {
		String key = context.getKey();
		String token = lockValues.remove(key);
		if (token != null) {
			// Lua 스크립트를 통해 안전하게 키 삭제
			stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
		}
	}
}

/**
 * Lua 스크립트는 Redis 서버 내부에 내장된 Lua 인터프리터가 실행하는 스크립트 코드입니다. 보통 다음과 같은 이유로 사용합니다:
 * <br>
 * 원자성(Atomicity) 보장
 * Redis는 단일 명령 단위로 원자성을 보장하지만, 여러 명령을 조합하면 중간에 다른 클라이언트가 끼어들 수 있습니다.
 * → Lua 스크립트 안에서 여러 Redis 명령을 순차적으로 실행하면 “스크립트 전체가 하나의 원자적 작업” 으로 처리됩니다.
 * <br>
 * 네트워크 왕복 감소
 * 클라이언트와 Redis 간에 명령을 여러 번 주고받지 않고, 스크립트 한 번 전송으로 복잡한 로직을 서버 단에서 수행합니다.
 * → 네트워크 지연(Latency)을 줄이고 처리량(Throughput)을 높일 수 있습니다.
 * <br>
 * 조건부 연산 및 복잡한 로직 수행
 * IF…THEN…ELSE, 반복문, 변수 등을 활용해 “키 값이 일치할 때만 삭제” 같은 조건부 로직을 간단히 표현할 수 있습니다.
 * <br>
 */
