package kr.hhplus.be.server.global.cache;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.benmanes.caffeine.cache.Cache;

import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.command.ConcertDateSearchCommand;
import kr.hhplus.be.server.application.concert.info.ConcertScheduleInfo;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleCashRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
public class RedisAndCacheTest {

	@Autowired
	private ConcertScheduleCashRepository concertScheduleCashRepository;

	@Autowired
	private ConcertJpaRepository concertJpaRepository;

	@Autowired
	private ConcertService concertService;
	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;

	@Autowired
	private Cache<String, List<ConcertScheduleInfo>> localCache;

	@Test
	void redis에_지정한_값들이_들어간뒤_만료시간이_지나면_제거된다() throws InterruptedException {
		// arrange
		String redisKey = "test-key";
		String redisValue = "test-value";
		long expireTime = 10L;
		// act
		concertScheduleCashRepository.put(redisKey, redisValue, expireTime);
		String value = concertScheduleCashRepository.get(redisKey, String.class);
		// assert
		assertThat(value).isEqualTo(redisValue);

		Thread.sleep((expireTime + 1) * 1000);
		assertThat(concertScheduleCashRepository.get(redisKey, String.class))
			.isNull();
	}

	@Test
	void 콘서트_스케줄_캐싱이_정상적으로_이루어진다() {
		// arrange
		Concert concert = concertJpaRepository.save(
			Concert.builder()
				.concertTitle("윤하 콘서트")
				.artistName("윤하")
				.build()
		);
		ConcertSchedule schedule1 = ConcertSchedule.builder()
			.concertDate(LocalDate.of(2025, 6, 20))
			.venue("성균관대학교")
			.status(ConcertScheduleStatus.AVAILABLE)
			.createdAt(LocalDateTime.now())
			.concert(concert)
			.build();
		concertScheduleJpaRepository.save(schedule1);

		ConcertDateSearchCommand command = new ConcertDateSearchCommand(
			LocalDate.of(2025, 6, 1),
			LocalDate.of(2025, 9, 22),
			1,
			30
		);

		String key = concert.getId()
			+ "::schedule::"
			+ command.startDate().toString()
			+ "::"
			+ command.endDate().toString();

		// ② 초기 캐시 비어 있음 검증
		assertThat(localCache.getIfPresent(key))
			.as("초기 로컬 캐시는 비어 있어야 합니다")
			.isNull();
		assertThat(concertScheduleCashRepository.get(key, ConcertScheduleInfo[].class))
			.as("초기 Redis 캐시는 비어 있어야 합니다")
			.isNull();

		// when
		List<ConcertScheduleInfo> info = concertService.searchDate(concert.getId(), command);

		// then: 서비스 반환값 검증
		assertThat(info)
			.hasSize(1)
			.extracting(ConcertScheduleInfo::concertDate)
			.containsExactly(LocalDate.of(2025, 6, 20));

		// ③ Redis 캐시 저장 여부 검증
		ConcertScheduleInfo[] cached =
			concertScheduleCashRepository.get(key, ConcertScheduleInfo[].class);
		assertThat(cached)
			.as("서비스 호출 후 Redis 캐시에 데이터가 저장되어야 합니다")
			.isNotNull()
			.extracting(ConcertScheduleInfo::concertDate)
			.containsExactly(LocalDate.of(2025, 6, 20));

		// ④ 로컬 캐시 저장 여부 검증
		List<ConcertScheduleInfo> localCached = localCache.getIfPresent(key);
		assertThat(localCached)
			.as("서비스 호출 후 로컬 캐시에 데이터가 저장되어야 합니다")
			.isNotNull()
			.extracting(ConcertScheduleInfo::concertDate)
			.containsExactly(LocalDate.of(2025, 6, 20));
	}
}
