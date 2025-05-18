package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.global.support.event.SearchRankEvent;
import kr.hhplus.be.server.global.support.event.SearchRankListener;
import kr.hhplus.be.server.global.support.event.SearchRankListenerContext;

@ExtendWith(MockitoExtension.class)
public class SearchRankEventUnitTest {

	@Mock
	private ConcertRankRepository concertRankRepository;

	@Mock
	private ConcertRepository concertRepository;

	@InjectMocks
	private SearchRankListener listener;

	private final Object source = new Object(); // 이벤트 소스 객체

	private final ZoneId zone = ZoneId.systemDefault();

	@Test
	void 좌석이_매진되었을_경우_이벤트_실행시_정상적으로_시간을_구한다() throws JsonProcessingException {
		// arrange
		Concert concert = Concert.builder()
			.id(1L)
			.artistName("윤하")
			.concertTitle("윤하 콘서트")
			.build();
		LocalDate dateTime = LocalDate.of(2025, 6, 30);
		ConcertSchedule schedule = ConcertSchedule.of(
			"성균관대", dateTime, ConcertScheduleStatus.AVAILABLE,
			LocalDateTime.now(), concert
		);
		ReflectionTestUtils.setField(schedule, "id", 1L);
		SearchRankEvent event = new SearchRankEvent(source, schedule.getId());

		given(concertRepository.getAvailableConcertSeat(schedule.getId())).willReturn(0L);
		given(concertRepository.findConcertSchedule(schedule.getId())).willReturn(Optional.of(schedule));
		given(concertRepository.findByConcertId(schedule.getConcert().getId())).willReturn(
			Optional.ofNullable(concert));

		Instant start = schedule.getConcertDate()
			.atStartOfDay(zone)   // ZonedDateTime 생성
			.toInstant();

		long expectedMillis = Duration.between(start, Instant.now()).toMillis();

		SearchRankListenerContext context = new SearchRankListenerContext(concert.getConcertTitle(),
			schedule.getConcertDate().toString());

		// act
		listener.checkedSellout(event);

		// assert
		ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		verify(concertRankRepository)
			.saveSelloutTime(eq(context.objectToString()), captor.capture());

		long actualMillis = captor.getValue();
		assertThat(actualMillis)
			.as("테스트 코드 실행과 이벤트 호출 시점간의 차이 보완")
			.isBetween(expectedMillis, actualMillis + 10);
	}

}
