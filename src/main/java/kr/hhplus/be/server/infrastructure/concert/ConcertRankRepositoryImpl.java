package kr.hhplus.be.server.infrastructure.concert;

import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ConcertRankRepositoryImpl implements ConcertRankRepository {
	private final StringRedisTemplate redis; // Redis 연산 템플릿 주입
	private final String key = "concert:selloutTime";

	// 좌석이 매진 되었을때까지 걸린 밀리초를 스코어로 사용함.
	// value에 콘서트 명과, 날짜를 기입하여 레디스에서 바로 조회하도록함.
	@Override
	public void saveSelloutTime(String context, long selloutMillis) {
		redis.opsForZSet().add(key, context, selloutMillis);
	}

	// 1~5순위를 구함
	@Override
	public Set<String> top5ConcertSchedule() {
		return redis.opsForZSet().range(key, 0, 4);
	}

	@Override
	public void resetRank() {
		redis.delete(key);
	}
}
