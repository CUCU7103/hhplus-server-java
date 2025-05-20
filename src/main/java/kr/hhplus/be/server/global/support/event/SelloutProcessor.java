package kr.hhplus.be.server.global.support.event;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

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
public class SelloutProcessor {
	private final ConcertRepository concertRepository;
	private final ConcertRankRepository concertRankRepository;
	private final Clock clock;

	/**
	 * 예외 발생 시 DB 백업용 엔터티를 생성합니다.
	 */

	/**
	 * Context 객체를 생성합니다.
	 */
	public SearchRankListenerContext buildContext(long concertScheduleId) {
		ConcertSchedule schedule = concertRepository.findConcertSchedule(concertScheduleId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));
		Concert concert = concertRepository.findByConcertId(schedule.getConcert().getId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));
		return new SearchRankListenerContext(
			concert.getConcertTitle(),
			schedule.getConcertDate().toString()
		);
	}

	/**
	 * 매진까지 걸린 시간(밀리초)을 계산합니다.
	 */
	public long calculateMillis(long concertScheduleId) {
		ConcertSchedule schedule = concertRepository.findConcertSchedule(concertScheduleId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));
		Instant openInstant = schedule.getConcertOpenDate()
			.atZone(ZoneId.systemDefault())
			.toInstant();
		// Instant.now(clock) 로 호출 시점을 고정
		return Duration.between(openInstant, Instant.now(clock)).toMillis();
	}

}
