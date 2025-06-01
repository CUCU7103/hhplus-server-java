package kr.hhplus.be.server.application.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DemoProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;

	public DemoProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendMessage1(String topic1, String msg) {
		kafkaTemplate.send(topic1, msg);
	}

	public void sendMessage2(String topic2, String msg) {
		kafkaTemplate.send(topic2, msg);
	}
}
