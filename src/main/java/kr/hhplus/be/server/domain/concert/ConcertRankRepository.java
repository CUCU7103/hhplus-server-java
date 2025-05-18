package kr.hhplus.be.server.domain.concert;

import java.util.Set;

public interface ConcertRankRepository {
	void saveSelloutTime(String context, long selloutMillis); // 판매율만 저장

	Set<String> top5ConcertSchedule();

	void resetRank();
}
