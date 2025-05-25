package kr.hhplus.be.server.infrastructure.concert.rank;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistory;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistoryRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ConcertRankingHistoryRepositoryImpl implements ConcertRankingHistoryRepository {

	private final ConcertRankingHistoryJpaRepository concertRankingHistoryJpaRepository;

	@Override
	public List<ConcertRankingHistory> saveAll(List<ConcertRankingHistory> rankingHistories) {
		return concertRankingHistoryJpaRepository.saveAll(rankingHistories);
	}

	@Override
	public ConcertRankingHistory saveBackup(ConcertRankingHistory concertRankingHistory) {
		return concertRankingHistoryJpaRepository.save(concertRankingHistory);
	}
}
