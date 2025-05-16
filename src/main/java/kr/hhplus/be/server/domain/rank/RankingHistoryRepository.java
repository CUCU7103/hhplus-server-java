package kr.hhplus.be.server.domain.rank;

import java.util.List;

public interface RankingHistoryRepository {

	List<RankingHistory> saveAll(List<RankingHistory> rankingHistories);
}
