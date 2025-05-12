package kr.hhplus.be.server.global.support.lock.model;

import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.global.support.lock.redis.RedisPubSubLockStrategy;
import kr.hhplus.be.server.global.support.lock.redis.RedisSimpleLockStrategy;
import kr.hhplus.be.server.global.support.lock.redis.RedisSpinLockStrategy;
import lombok.RequiredArgsConstructor;

@Component // 스프링 빈으로 등록한다
@RequiredArgsConstructor
public class LockFactoryImpl implements LockFactory {

	private final StringRedisTemplate redisTemplate;
	private final RedissonClient redissonClient;
	private final TimeProvider timeProvider;

	@Override
	public LockStrategy getLock(LockType lockType) { // 요청한 락 타입에 맞는 구현체 반환
		return switch (lockType) {
			case REDIS_SPIN -> new RedisSpinLockStrategy(redisTemplate, timeProvider); // Spin Lock 객체 생성
			case REDIS_SIMPLE -> new RedisSimpleLockStrategy(redisTemplate); // Simple Lock 객체 생성
			case REDIS_PUBSUB -> new RedisPubSubLockStrategy(redissonClient); // Pub/Sub Lock 객체 생성
			default -> throw new IllegalArgumentException("Unknown lock type: " + lockType); // 잘못된 타입 예외
		};
	}
}
