package kr.hhplus.be.server.infrastructure.concert;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.payment.event.RankContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ConcertRankRepositoryImpl implements ConcertRankRepository {
	private final RedisTemplate<String, RankContext> redisTemplate;
	private final String key = "concert:selloutTime";

	// 좌석이 매진 되었을때까지 걸린 밀리초를 스코어로 사용함.
	// value에 콘서트 명과, 날짜를 기입하여 레디스에서 바로 조회하도록함.
	@Override
	public boolean saveSelloutTime(RankContext context, long selloutMillis) {
		// 데이터 저장
		Boolean result = redisTemplate.opsForZSet().add(key, context, selloutMillis);
		// 저장 결과 확인 (add 메서드는 Boolean 타입 반환)
		if (result != null && result) {
			log.info("데이터가 성공적으로 저장되었습니다. key: {}, member: {}, score: {}", key, context, selloutMillis);
			return true;
		} else {
			log.warn("데이터 저장에 실패했거나 이미 존재하는 데이터입니다. key: {}, member: {}", key, context);
			return false;
		}
	}

	// 1~5순위를 구함
	@Override
	public Set<RankContext> top5ConcertSchedule() {
		return redisTemplate.opsForZSet().range(key, 0, 4);
	}

	@Override
	public void resetRank() {
		redisTemplate.delete(key);
	}

}
