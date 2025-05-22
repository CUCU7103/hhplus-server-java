package kr.hhplus.be.server.domain.concert.rank;

import java.util.List;

public interface ConcertRankingHistoryRepository {

	List<ConcertRankingHistory> saveAll(List<ConcertRankingHistory> rankingHistories);

	ConcertRankingHistory saveBackup(ConcertRankingHistory concertRankingHistory);
}
