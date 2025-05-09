package kr.hhplus.be.server.infrastructure.concert;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleCashRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConcertScheduleCashRepositoryImpl implements ConcertScheduleCashRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public <T> T get(String key, Class<T> classType) {
		ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
		Object value = valueOperations.get(key);
		if (value == null) {
			return null;
		}

		try {
			// Redis에 저장된 값은 항상 문자열 형태
			return objectMapper.readValue((String)value, classType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("캐시된 값을 변환하는 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	@Override
	public void put(String key, Object value, long expireTime) {
		ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
		try {
			// 객체를 JSON 문자열로 직렬화
			String jsonValue = objectMapper.writeValueAsString(value);
			valueOperations.set(key, jsonValue);
			// 만료 시간 설정 (초 단위)
			if (expireTime <= 0) {
				throw new IllegalAccessException("만료시간을 설정하세요");
			}
			redisTemplate.expire(key, expireTime, TimeUnit.MINUTES);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("값을 캐시에 저장하는 중 오류가 발생했습니다: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
