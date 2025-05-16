package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.global.support.event.SearchRankEvent;
import kr.hhplus.be.server.global.support.event.SearchRankListener;
import kr.hhplus.be.server.global.support.event.SearchRankListenerContext;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import lombok.extern.slf4j.Slf4j;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class SearchRankEventIntegrationTest {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private SearchRankListener searchRankListener; // 주입 시도

	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;

	@Autowired
	private ConcertRepository concertRepository;

	@Autowired
	private ConcertSeatJpaRepository concertSeatJpaRepository;

	@Autowired
	private ConcertRankRepository rankRepository;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ConcertJpaRepository concertJpaRepository;

	@Test
	void 리스너_등록_확인() {
		assertThat(searchRankListener).isNotNull(); // 리스너가 등록되었는지 확인
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void 좌석_매진인_경우_이벤트_실행시_정상적으로_레디스에_값이_저장되어진다() throws JsonProcessingException {
		// arrange
		Concert concert = Concert.builder()
			.artistName("윤하")
			.concertTitle("윤하 콘서트")
			.build();
		concertJpaRepository.save(concert);

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 6, 10))
				.venue("서울대학교")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.of(2025, 3, 10, 0, 0, 0))
				.concert(concert)
				.build()
		);
		// 좌석 50개 예약 처리
		for (int i = 1; i <= 50; i++) {
			ConcertSeat seat1 = concertSeatJpaRepository.save(
				ConcertSeat.builder()
					.concertSchedule(schedule)
					.section("A")
					.seatNumber(i)
					.status(ConcertSeatStatus.BOOKED)
					.build()
			);
		}

		// act
		// 리스너 메소드 직접 호출
		searchRankListener.checkedSellout(new SearchRankEvent(this, schedule.getId()));

		// assert
		await()
			.pollInterval(500, TimeUnit.MILLISECONDS) // 폴링 간격 설정
			.atMost(5, TimeUnit.SECONDS) // 대기 시간 증가
			.untilAsserted(() -> {
				// assert
				Boolean hasKey = redisTemplate.hasKey("concert:selloutTime");
				log.info("Redis 키 존재 여부 {}", hasKey);
				assertThat(hasKey).isTrue();
				SearchRankListenerContext context = new SearchRankListenerContext(concert.getConcertTitle(),
					schedule.getConcertDate().toString());

				// 저장된 값이 있는지 확인
				Set<String> rangeResult = redisTemplate.opsForZSet().range("concert:selloutTime", 0, -1);
				log.info("redis에 저장된 값 확인 {}", rangeResult);
				assertThat(rangeResult).isNotEmpty();
				assertThat(rangeResult.size()).isNotZero();
				// assertThat(rangeResult).isEqualTo(Set.of(context.objectToString()));

			});

	}

}
