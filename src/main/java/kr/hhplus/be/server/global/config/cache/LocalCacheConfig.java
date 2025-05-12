package kr.hhplus.be.server.global.config.cache;

import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import kr.hhplus.be.server.application.concert.info.ConcertScheduleInfo;

@Configuration
public class LocalCacheConfig {
	/**
	 * L1 캐시: Caffeine 인메모리 캐시
	 */
	@Bean
	public Cache<String, List<ConcertScheduleInfo>> caffeineCache() {
		return Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofSeconds(20))   // 30초 TTL
			.maximumSize(20_000)                       // 최대 20,000개 엔트리
			.build();
	}
}
