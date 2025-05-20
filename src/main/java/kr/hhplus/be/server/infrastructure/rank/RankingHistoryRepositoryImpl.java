package kr.hhplus.be.server.infrastructure.rank;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.rank.RankingHistory;
import kr.hhplus.be.server.domain.rank.RankingHistoryRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RankingHistoryRepositoryImpl implements RankingHistoryRepository {

	private final RankingHistoryJpaRepository rankingHistoryJpaRepository;

	@Override
	public List<RankingHistory> saveAll(List<RankingHistory> rankingHistories) {
		return rankingHistoryJpaRepository.saveAll(rankingHistories);
	}

	@Override
	public RankingHistory saveBackup(RankingHistory rankingHistory) {
		return rankingHistoryJpaRepository.save(rankingHistory);
	}
}
