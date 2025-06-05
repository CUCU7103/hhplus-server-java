package kr.hhplus.be.server.global.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.domain.payment.event.RankContext;

@Configuration
public class RedisConfig {

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
	public RedisTemplate<String, RankContext> searchRankRedisTemplate(
		RedisConnectionFactory connectionFactory,
		ObjectMapper objectMapper) {

		RedisTemplate<String, RankContext> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());

		JavaType javaType = objectMapper.getTypeFactory().constructType(RankContext.class);
		Jackson2JsonRedisSerializer<RankContext> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, javaType);

		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setDefaultSerializer(serializer);

		template.afterPropertiesSet();
		return template;
	}

}
