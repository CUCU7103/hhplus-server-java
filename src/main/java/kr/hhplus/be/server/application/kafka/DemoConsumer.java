package kr.hhplus.be.server.application.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DemoConsumer {

	@KafkaListener(topics = "demo-topic1", groupId = "demo-group1")
	public void listen1(String message) {
		log.info("▶ 수신된 메시지-1: {}", message);
		// 실제로는 DB 저장, 외부 API 호출 등 비즈니스 로직 수행
	}

	@KafkaListener(topics = "demo-topic2", groupId = "demo-group2")
	public void listen2(String message) {
		log.info("▶ 수신된 메시지-2: {}", message);
		// 실제로는 DB 저장, 외부 API 호출 등 비즈니스 로직 수행
	}

}
