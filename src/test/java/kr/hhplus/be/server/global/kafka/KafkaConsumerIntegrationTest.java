package kr.hhplus.be.server.global.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.event.RankContext;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 트랜잭션 분리
public class KafkaConsumerIntegrationTest {

	@Autowired
	private KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;

	@Autowired
	private ConcertSeatJpaRepository concertSeatJpaRepository;

	@Autowired
	private ConcertRankingRepository rankRepository;

	@Autowired
	@Qualifier("searchRankRedisTemplate")
	private RedisTemplate<String, RankContext> redisTemplate;

	@Autowired
	private ConcertJpaRepository concertJpaRepository;

	@Autowired
	private ConsumerFactory<String, PaymentCompletedEvent> consumerFactory;  // ← 자동 주입

	private Consumer<String, PaymentCompletedEvent> dltConsumer;

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
		// Redis 초기화
		log.info("redis 초기화");
		redisTemplate.delete("concert:selloutTime");
		// ----------------------------------------------------
		// ConsumerFactory로부터 DLT 전용 Consumer 생성
		// ----------------------------------------------------
		dltConsumer = consumerFactory.createConsumer("test-dlt-consumer", null);
		dltConsumer.subscribe(Collections.singletonList("payment-completed-dlt"));

		await().atMost(5, TimeUnit.SECONDS)
			.pollInterval(500, TimeUnit.MILLISECONDS)
			.until(() -> {
				// 컨슈머가 준비될 때까지 대기
				log.info("카프카 컨슈머 준비 대기 중...");
				return true;
			});
	}

	@AfterEach
	void tearDown() {
		// 테스트 정리
		redisTemplate.delete("concert:selloutTime");

		if (dltConsumer != null) {
			try {
				log.info("DLT 컨슈머 정리 중...");
				dltConsumer.unsubscribe();
				dltConsumer.close(Duration.ofSeconds(5));
				log.info("DLT 컨슈머 정리 완료");
			} catch (Exception e) {
				log.warn("DLT 컨슈머 정리 중 예외 발생: {}", e.getMessage());
			} finally {
				dltConsumer = null;
			}
		}
		// restore RedisTemplate to rankRepository
		ReflectionTestUtils.setField(rankRepository, "redisTemplate", redisTemplate);
	}

	@Test
	void Producer가_메시지_발행_후_Consumer가_로직을_정상적으로_수행한다() {
		// 1) arrange: DB에 콘서트, 스케줄, 그리고 매진된 좌석 50개 저장
		Concert concert = concertJpaRepository.save(
			Concert.builder()
				.artistName("윤하")
				.concertTitle("윤하 콘서트")
				.build()
		);

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concert(concert)
				.concertDate(LocalDate.of(2025, 6, 10))
				.venue("서울대학교")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.of(2025, 3, 10, 0, 0))
				.concertOpenDate(LocalDateTime.of(2025, 6, 20, 10, 0))
				.build()
		);

		// 50개 좌석을 BOOKED 상태로 생성 (매진 상황)
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

		// 2) act: Kafka에 결제 완료 이벤트 발행
		PaymentCompletedEvent event = PaymentCompletedEvent.create(
			schedule.getId(),
			concert.getConcertTitle(),
			schedule.getConcertDate(),
			schedule.getConcertOpenDate(),
			1L, // userId
			1L, // reservationId
			BigDecimal.valueOf(1000L),
			LocalDateTime.now()
		);

		// TestcontainersConfiguration의 KafkaContainer로 메시지 전송
		kafkaTemplate.send("payment-completed", event);

		// 3) assert: 비동기 처리 결과 검증
		await()
			.atMost(30, TimeUnit.SECONDS)
			.pollInterval(Duration.ofSeconds(1))
			.untilAsserted(() -> {
				boolean hasKey = redisTemplate.hasKey("concert:selloutTime");
				log.info("Redis 키 존재 여부 확인: {}", hasKey);

				if (hasKey) {
					Set<RankContext> entries = redisTemplate.opsForZSet()
						.range("concert:selloutTime", 0, -1);
					log.info("Redis 저장된 데이터: {}", entries);
				}

				assertThat(hasKey)
					.as("Redis에 selloutTime 키가 존재해야 합니다")
					.isTrue();

				Set<RankContext> entries = redisTemplate.opsForZSet()
					.range("concert:selloutTime", 0, -1);

				assertThat(entries)
					.as("Redis에 랭킹 데이터가 저장되어야 합니다")
					.isNotEmpty();

				// 추가 검증
				RankContext savedContext = entries.iterator().next();
				assertThat(savedContext.concertName())
					.isEqualTo("윤하 콘서트");

				log.info("테스트 검증 완료: {}", savedContext);
			});
	}

	@Test
	void 레디스_연결_실패로_인한_DLT_처리() throws Exception {
		// arrange: 정상적인 매진 상황 데이터 준비
		Concert concert = concertJpaRepository.save(
			Concert.builder()
				.artistName("윤하")
				.concertTitle("윤하 콘서트")
				.build()
		);

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concert(concert)
				.concertDate(LocalDate.of(2025, 6, 10))
				.venue("서울대학교")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.of(2025, 3, 10, 0, 0))
				.concertOpenDate(LocalDateTime.of(2025, 6, 20, 10, 0))
				.build()
		);

		// 50개 좌석을 BOOKED 상태로 생성 (매진 상황)
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

		// 레디스 서비스를 강제로 중지하여 연결 실패 상황 유도
		// TestContainers의 Redis 컨테이너를 중지시켜 실제 연결 실패 상황 모킹
		// 또는 잘못된 Redis 키를 사용하여 예외 상황 유도하는 방법 사용

		// 방법 1: Redis 컨테이너 중지 (TestContainers 사용 시)
		ReflectionTestUtils.setField(rankRepository, "redisTemplate", null);

		PaymentCompletedEvent event = PaymentCompletedEvent.create(
			schedule.getId(),
			concert.getConcertTitle(),
			schedule.getConcertDate(),
			schedule.getConcertOpenDate(),
			1L, // userId
			1L, // reservationId
			BigDecimal.valueOf(1000L),
			LocalDateTime.now()
		);

		// act: 레디스 연결 실패 상황에서 메시지 발행
		kafkaTemplate.send("payment-completed", event);

		// assert: DLT로 메시지가 전송되었는지 확인
		await()
			.atMost(30, TimeUnit.SECONDS)
			.pollInterval(Duration.ofSeconds(1))
			.untilAsserted(() -> {
				ConsumerRecord<String, PaymentCompletedEvent> record =
					KafkaTestUtils.getSingleRecord(dltConsumer, "payment-completed-dlt");

				assertThat(record.value().scheduleId()).isEqualTo(schedule.getId());
				assertThat(record.value().concertTitle()).isEqualTo("윤하 콘서트");

				log.info("레디스 연결 실패로 인한 DLT 처리 확인: scheduleId={}, concertTitle={}",
					record.value().scheduleId(), record.value().concertTitle());
			});
	}

}
