package kr.hhplus.be.server.global.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.global.support.event.SearchRankListenerContext;

@Configuration
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(stringRedisSerializer);
		redisTemplate.setValueSerializer(stringRedisSerializer);
		return redisTemplate;
	}

	// 커스텀을 위해서 개별적인 탬플릿 사용
	@Bean(name = "searchRankRedisTemplate")
	public RedisTemplate<String, SearchRankListenerContext> searchRankRedisTemplate(
		RedisConnectionFactory connectionFactory,
		ObjectMapper objectMapper) {
		RedisTemplate<String, SearchRankListenerContext> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 키는 String
		template.setKeySerializer(new StringRedisSerializer());

		// 값은 Jackson2JsonRedisSerializer 사용
		JavaType javaType = objectMapper.getTypeFactory()
			.constructType(SearchRankListenerContext.class);
		Jackson2JsonRedisSerializer<SearchRankListenerContext> serializer =
			new Jackson2JsonRedisSerializer<>(objectMapper, javaType);

		template.setValueSerializer(serializer);

		// ZSet 연산을 위한 직렬화 설정 추가
		template.setHashValueSerializer(serializer);
		template.setHashKeySerializer(new StringRedisSerializer());

		// 중요: ZSet 스코어 값은 기본 직렬화 사용
		template.setDefaultSerializer(serializer);

		template.afterPropertiesSet();
		return template;
	}

}
