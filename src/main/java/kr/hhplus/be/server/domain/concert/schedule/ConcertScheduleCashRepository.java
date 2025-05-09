package kr.hhplus.be.server.domain.concert.schedule;

public interface ConcertScheduleCashRepository {
	// 캐시에서 데이터를 조회
	<T> T get(String key, Class<T> classType);

	// 캐시에 데이터 저장 (만료 시간 지정)
	void put(String key, Object value, long expireTime);

}
