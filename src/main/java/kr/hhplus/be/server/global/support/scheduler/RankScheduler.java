package kr.hhplus.be.server.global.support.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.application.rank.RankingHistoryService;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.rank.RankingHistory;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RankScheduler {

	// redis에서 하루동안 저장된 1~5위 랭킹을 db에 저장하면서 redis에서 지운다.
	private final ConcertRankRepository rankRepository;
	private final RankingHistoryService rankingHistoryService;

	@Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
	public void resetRedisRank() {
		try {
			log.info("redis 랭킹 초기화 시작: {}", LocalDateTime.now());
			rankRepository.resetRank();
		} catch (Exception e) {
			log.error("[스케줄러] 랭킹 초기화 중 오류 발생", e);
			throw new CustomException(CustomErrorCode.RANK_RESET_FAIL);
		}
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void saveDailyRank() {
		log.info("[스케줄러] 자정 랭킹 저장 시작 – {}", LocalDateTime.now());
		try {
			List<RankingHistory> saved = rankingHistoryService.persistTopRankingsToDB();
			log.info("[스케줄러] 자정 랭킹 저장 완료 – 총 {}건", saved.size());
		} catch (Exception ex) {
			log.error("[스케줄러] 랭킹 저장 중 오류 발생", ex);
			throw new CustomException(CustomErrorCode.RANK_SAVE_FAIL);
		}
	}

}
