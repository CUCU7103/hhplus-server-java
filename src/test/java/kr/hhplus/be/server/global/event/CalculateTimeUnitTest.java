package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.concert.CalculateTime;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.infrastructure.payment.SpringPaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CalculateTimeUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@InjectMocks
	private CalculateTime calculateTime;

	@Spy
	Clock clock = Clock.fixed(Instant.parse("2025-06-20T10:00:00Z"), ZoneId.systemDefault());

	@Test
	void calculateMillis는_콘서트_오픈시간부터_현재까지의_시간을_계산한다() {

		final Instant FIXED_NOW = Instant.parse("2025-06-20T10:00:00Z");
		final Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneId.systemDefault());

		Instant openInstant = Instant.parse("2025-06-20T09:00:00Z");
		LocalDate concertDate = LocalDate.of(2025, 10, 30);

		SpringPaymentEventPublisher event = mock(SpringPaymentEventPublisher.class);

		Concert concert = Concert.builder().id(1L).artistName("윤하").concertTitle("윤하 콘서트").build();
		ConcertSchedule schedule = ConcertSchedule.createBackup("서울대", concertDate, ConcertScheduleStatus.AVAILABLE,
			LocalDateTime.now(), concert, LocalDateTime.ofInstant(openInstant, ZoneId.systemDefault()));

		ReflectionTestUtils.setField(schedule, "id", 1L);

		long actualMillis = calculateTime.calculateMillis(schedule.getConcertOpenDate());

		long expectedMillis = Duration.between(openInstant, FIXED_NOW).toMillis();
		// 약간의 실행 오버헤드를 고려해 100ms 허용 오차 설정
		assertThat(actualMillis).isCloseTo(expectedMillis, within(100L));

	}

}
