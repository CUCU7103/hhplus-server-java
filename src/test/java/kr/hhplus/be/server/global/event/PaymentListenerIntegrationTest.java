package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.event.RankContext;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import kr.hhplus.be.server.infrastructure.rank.RankingHistoryJpaRepository;
import kr.hhplus.be.server.presentation.payment.PaymentListener;
import lombok.extern.slf4j.Slf4j;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class PaymentListenerIntegrationTest {

	@Autowired
	private PaymentListener paymentListener; // 주입 시도

	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;

	@Autowired
	private RankingHistoryJpaRepository rankingHistoryJpaRepository;

	@Autowired
	private ConcertSeatJpaRepository concertSeatJpaRepository;

	@Autowired
	private ConcertRankRepository rankRepository;

	@Autowired
	@Qualifier("searchRankRedisTemplate")
	private RedisTemplate<String, RankContext> redisTemplate;

	@Autowired
	private ConcertJpaRepository concertJpaRepository;

	@TestConfiguration
	static class FixedClockConfig {
		/**
		 * 기존 ClockConfig 의 'clock' 빈과 충돌을 피하기 위해
		 * 메서드명을 fixedClock 으로 바꾸고, @Primary 로 우선순위를 높입니다.
		 */
		@Bean
		@Primary
		public Clock fixedClock() {
			return Clock.fixed(
				Instant.parse("2025-06-20T09:00:00Z"),
				ZoneId.systemDefault()
			);
		}
	}

	@BeforeEach
	void setUp() {
		log.info("기존 데이터 초기화");
		redisTemplate.delete("concert:selloutTime");
	}

	@Test
	void 리스너_등록_확인() {
		assertThat(paymentListener).isNotNull(); // 리스너가 등록되었는지 확인
	}

	// 리스너 자체를 통합 테스트
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void 좌석_매진인_경우_이벤트_실행시_정상적으로_레디스에_값이_저장되어진다() throws InterruptedException {
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
				.concertOpenDate(LocalDateTime.of(2025, 6, 20, 10, 0))
				.build()
		);
		// 좌석 50개 예약 처리
		for (int i = 1; i <= 50; i++) {
			concertSeatJpaRepository.save(
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
		paymentListener.rankingUpdateListener(
			PaymentCompletedEvent.create(schedule.getId(), concert.getConcertTitle(), schedule.getConcertDate(),
				schedule.getConcertOpenDate(), 1L, 1L, BigDecimal.valueOf(1000L), LocalDateTime.now()));

		// assert - 비동기 작업 완료를 기다립니다 (Awaitility 라이브러리 사용)
		Awaitility.await()
			.atMost(10, TimeUnit.SECONDS)  // 최대 10초 대기
			.pollInterval(500, TimeUnit.MILLISECONDS)  // 0.5초마다 확인
			.untilAsserted(() -> {
				// Redis에 키가 존재하는지 확인
				Boolean hasKey = redisTemplate.hasKey("concert:selloutTime");
				log.info("Redis 키 존재 여부 {}", hasKey);
				assertThat(hasKey).isTrue();

				// 저장된 값이 있는지 확인
				Set<RankContext> rangeResult = redisTemplate.opsForZSet()
					.range("concert:selloutTime", 0, -1);
				log.info("redis에 저장된 값 확인 {}", rangeResult);
				assertThat(rangeResult).isNotEmpty();
				assertThat(rangeResult.size()).isNotZero();
			});
	}

}
