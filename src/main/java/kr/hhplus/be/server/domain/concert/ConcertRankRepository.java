package kr.hhplus.be.server.domain.concert;

import java.util.Set;

import kr.hhplus.be.server.domain.payment.event.RankContext;

public interface ConcertRankRepository {
	boolean saveSelloutTime(RankContext context, long millis);

	Set<RankContext> top5ConcertSchedule();

	void resetRank();

}
