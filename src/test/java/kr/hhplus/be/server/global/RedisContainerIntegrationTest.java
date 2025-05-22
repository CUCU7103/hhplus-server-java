package kr.hhplus.be.server.global;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.global.support.event.SearchRankListenerContext;

// TestcontainersConfiguration 를 스프링 컨텍스트에 등록
@SpringBootTest
@ActiveProfiles("test")
public class RedisContainerIntegrationTest {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	@Qualifier("searchRankRedisTemplate")
	private RedisTemplate<String, SearchRankListenerContext> redisTemplate;

	@Autowired
	private ConcertRankRepository concertRankRepository;

	@Test
	void redis에_값이_정상적으로_저장되고_조회되는지_테스트() {
		// given
		String key = "sample:key";
		String expectedValue = "hello-redis";

		// when
		stringRedisTemplate.opsForValue().set(key, expectedValue);
		String actualValue = stringRedisTemplate.opsForValue().get(key);

		// then
		assertThat(actualValue).isEqualTo(expectedValue);
	}

	@Test
	void redis_saveSelloutTime가_값을_성공적으로_저장한다() {
		String key = "concert:selloutTime";
		SearchRankListenerContext context = new SearchRankListenerContext("윤하 콘서트", "2025-10-25");
		long millis = 100L;

		redisTemplate.opsForZSet().add(key, context, millis);
	}

	@Test
	void saveSelloutTime가_값을_성공적으로_저장한다() {
		SearchRankListenerContext context = new SearchRankListenerContext("윤하 콘서트", "2025-10-25");
		long millis = 100L;
		concertRankRepository.saveSelloutTime(context, millis);
	}

}
