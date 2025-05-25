package kr.hhplus.be.server.domain.payment.event;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalculateTime {
	private final Clock clock;

	/**
	 * 매진까지 걸린 시간(밀리초)을 계산합니다.
	 */
	public long calculateMillis(LocalDateTime concertOpenDate) {
		Instant openInstant = concertOpenDate.atZone(ZoneId.systemDefault())
			.toInstant();
		// Instant.now(clock) 로 호출 시점을 고정
		return Duration.between(openInstant, Instant.now(clock)).toMillis();
	}

}
