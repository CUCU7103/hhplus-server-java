package kr.hhplus.be.server.global.config.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * 캐시 설정을 관리하는 설정 클래스
 * 다중 계층(Multi-Level) 캐싱 구조를 구현함
 * - 1차 캐시: Caffeine (로컬 메모리 기반, 빠른 접근 속도)
 * - 2차 캐시: Redis (분산 캐시 시스템, 여러 서버 간 데이터 공유)
 */
@Configuration
@EnableCaching  // 스프링의 캐싱 기능 활성화
@Slf4j
public class CacheConfig {

	/**
	 * Caffeine 캐시 매니저 빈 설정
	 * 1차 캐시로 사용됨 (인메모리 캐시)
	 *
	 * @return 설정된 Caffeine 캐시 매니저
	 */
	@Bean
	public CaffeineCacheManager caffeineCacheManager() {
		// "concertSchedule" 이름의 캐시 생성
		CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager("concertSchedule");

		// Caffeine 캐시 세부 설정
		caffeineCacheManager.setCaffeine(
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(2))  // 마지막 쓰기 후 20초 후 만료
				.maximumSize(8000)                         // 최대 8000개 항목 저장
				.recordStats() // 캐시 통계 기록 활성화
		);
		return caffeineCacheManager;
	}

	/**
	 * Redis 캐시 매니저 빈 설정
	 * 2차 캐시로 사용됨 (분산 캐시)
	 *
	 * @return 설정된 Redis 캐시 매니저
	 */
	@Bean
	public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
		// Redis 캐시 기본 설정
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(5)) // 캐시 항목 1분 후 만료
			.disableCachingNullValues(); // value가 null이면 캐싱하지 않음

		RedisCacheConfiguration longLived = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofDays(30))
			.disableCachingNullValues();

		Map<String, RedisCacheConfiguration> configs = new HashMap<>();
		configs.put("concertSchedule", longLived);
		// Redis 캐시 매니저 생성 및 반환
		return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(redisCacheConfiguration).build();
	}

	/**
	 * 복합 캐시 매니저 빈 설정
	 * Caffeine과 Redis 캐시를 결합하여 다중 계층 캐싱 제공
	 * - 요청 시 Caffeine 캐시 먼저 확인 후 없으면 Redis 캐시 확인
	 *
	 * @param caffeine Caffeine 캐시 매니저 (1차 캐시)
	 * @param redis Redis 캐시 매니저 (2차 캐시)
	 * @return 설정된 복합 캐시 매니저
	 */
	@Bean
	@Primary
	public CompositeCacheManager cacheManager(
		CaffeineCacheManager caffeine, RedisCacheManager redis) {
		// 복합 캐시 매니저 생성 (순서가 중요: Caffeine이 먼저 확인됨)
		CompositeCacheManager cm = new CompositeCacheManager(caffeine, redis);

		// 폴백(fallback) 동작 비활성화: 모든 캐시에 없을 경우 null 반환
		// true로 설정하면 캐시에 없을 경우 캐싱하지 않고 메서드 실행
		cm.setFallbackToNoOpCache(false);
		return cm;
	}
}
