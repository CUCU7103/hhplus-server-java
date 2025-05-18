package kr.hhplus.be.server.global.support.event;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchRankListener {
	private final ConcertRankRepository concertRankRepository;
	private final ConcertRepository concertRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	public void checkedSellout(SearchRankEvent event) throws JsonProcessingException {
		ZoneId zone = ZoneId.systemDefault();
		// 이용가능한 콘서트 좌석 조회
		final long availableSeats = concertRepository.getAvailableConcertSeat(event.getScheduleId());
		if (availableSeats != 0) { // 이용가능한 좌석이 0 이 아니라면
			return; // 아무것도 하지 않는다
		}
		// 이용가능한 좌석이 0이라면?
		// 콘서트 스케줄 오픈일자 - 지금 시간
		// value에 어떤 값?
		// 콘서트명, 날짜 2개만 포함시키자.
		ConcertSchedule schedule = concertRepository.findConcertSchedule(event.getScheduleId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));

		// Concert 엔티티를 직접 로드하여 초기화
		Concert concert = concertRepository.findByConcertId(schedule.getConcert().getId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		SearchRankListenerContext context = new SearchRankListenerContext(concert.getConcertTitle(),
			schedule.getConcertDate().toString());

		Instant sellOutTime = schedule.getConcertOpenDate().atZone(zone).toInstant();

		long millis = Duration.between(sellOutTime, Instant.now()).toMillis();

		concertRankRepository.saveSelloutTime(context.objectToString(), millis);
	}

}
