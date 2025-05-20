package kr.hhplus.be.server.domain.concert;

import java.util.Set;

import kr.hhplus.be.server.global.support.event.SearchRankListenerContext;

public interface ConcertRankRepository {
	boolean saveSelloutTime(SearchRankListenerContext context, long millis);

	Set<SearchRankListenerContext> top5ConcertSchedule();

	void resetRank();

}
