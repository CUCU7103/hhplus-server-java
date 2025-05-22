package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.global.support.event.PaymentEventPublisher;
import kr.hhplus.be.server.global.support.event.SearchRankListenerContext;
import kr.hhplus.be.server.global.support.event.SelloutProcessor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SelloutProcessorUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@InjectMocks
	private SelloutProcessor selloutProcessor;

	@Spy
	Clock clock = Clock.fixed(Instant.parse("2025-06-20T10:00:00Z"), ZoneId.systemDefault());

	@Test
	void Context_객체가_정상적으로_생성되어집니다() {
		// arrange
		LocalDate concertDate = LocalDate.of(2025, 10, 30);
		PaymentEventPublisher event = mock(PaymentEventPublisher.class);
		Concert concert = Concert.builder().id(1L).artistName("윤하").concertTitle("윤하 콘서트").build();
		ConcertSchedule schedule = ConcertSchedule.create("서울대", concertDate, ConcertScheduleStatus.AVAILABLE,
			LocalDateTime.now(), concert);

		ReflectionTestUtils.setField(schedule, "id", 1L);

		given(concertRepository.findConcertSchedule(schedule.getId())).willReturn(Optional.of(schedule));
		given(concertRepository.findByConcertId(schedule.getConcert().getId())).willReturn(Optional.of(concert));
		// act
		SearchRankListenerContext result = selloutProcessor.buildContext(schedule.getId());
		// assert
		assertThat(result).isNotNull();
		assertThat(result.concertDate()).isEqualTo(concertDate.toString());
		assertThat(result.concertName()).isEqualTo("윤하 콘서트");
	}

	@Test
	@DisplayName("buildContext는 정상적인 경우 컨텍스트 객체를 반환한다")
	void buildContext_WhenScheduleAndConcertExist_ReturnsContext() {
		// given
		PaymentEventPublisher event = mock(PaymentEventPublisher.class);
		ConcertSchedule schedule = mock(ConcertSchedule.class);
		Concert concert = mock(Concert.class);

		// 객체를 넣는 것 말고 stub을 사용해서 처리할 수있다.
		given(schedule.getId()).willReturn(1L);
		given(concertRepository.findConcertSchedule(1L)).willReturn(Optional.of(schedule));
		given(concert.getId()).willReturn(2L);
		given(concertRepository.findByConcertId(2L)).willReturn(Optional.of(concert));
		given(concert.getConcertTitle()).willReturn("테스트 콘서트");
		given(schedule.getConcertDate()).willReturn(LocalDate.of(2025, 5, 19));
		given(schedule.getConcert()).willReturn(concert);

		// when
		SearchRankListenerContext result = selloutProcessor.buildContext(schedule.getId());

		// then
		assertThat(result).isNotNull();
		assertThat(result.concertName()).isEqualTo("테스트 콘서트");

	}

	@Test
	void calculateMillis는_콘서트_오픈시간부터_현재까지의_시간을_계산한다() {

		final Instant FIXED_NOW = Instant.parse("2025-06-20T10:00:00Z");
		final Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneId.systemDefault());

		Instant openInstant = Instant.parse("2025-06-20T09:00:00Z");
		LocalDate concertDate = LocalDate.of(2025, 10, 30);

		PaymentEventPublisher event = mock(PaymentEventPublisher.class);

		Concert concert = Concert.builder().id(1L).artistName("윤하").concertTitle("윤하 콘서트").build();
		ConcertSchedule schedule = ConcertSchedule.createBackup("서울대", concertDate, ConcertScheduleStatus.AVAILABLE,
			LocalDateTime.now(), concert, LocalDateTime.ofInstant(openInstant, ZoneId.systemDefault()));

		ReflectionTestUtils.setField(schedule, "id", 1L);

		given(concertRepository.findConcertSchedule(schedule.getId())).willReturn(Optional.of(schedule));

		long actualMillis = selloutProcessor.calculateMillis(schedule.getId());

		long expectedMillis = Duration.between(openInstant, FIXED_NOW).toMillis();
		// 약간의 실행 오버헤드를 고려해 100ms 허용 오차 설정
		assertThat(actualMillis).isCloseTo(expectedMillis, within(100L));

	}

}
