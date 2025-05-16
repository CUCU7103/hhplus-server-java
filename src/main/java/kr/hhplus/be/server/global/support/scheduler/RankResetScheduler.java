package kr.hhplus.be.server.global.support.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RankResetScheduler {

	// redis에서 하루동안 저장된 1~5위 랭킹을 초기화
	private final ConcertRankRepository rankRepository;

	@Scheduled(cron = "0 0 0 * * *")
	public void checkAndReleaseHeldSeats() {
		log.info("redis 랭킹 초기화 {}", java.time.LocalDateTime.now());
		rankRepository.resetRanking();
		log.info("스케줄러 실행 완료.");
	}

}
