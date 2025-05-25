package kr.hhplus.be.server.domain.concert.rank;

import java.util.Set;

import kr.hhplus.be.server.domain.payment.event.RankContext;

public interface ConcertRankingRepository {
	boolean saveSelloutTime(RankContext context, long millis);

	Set<RankContext> top5ConcertSchedule();

	void resetRank();

}
