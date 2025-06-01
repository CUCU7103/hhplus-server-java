package kr.hhplus.be.server.infrastructure.payment;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.message.PaymentMessageProducer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaPaymentMessageProducer implements PaymentMessageProducer {
	private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
	final String PAYMENT_TOPIC = "payment-completed";

	@Override
	public void send(PaymentCompletedEvent event) {
		kafkaTemplate.send(PAYMENT_TOPIC, event);
	}

	@Override
	public void sendDlt(String topic, PaymentCompletedEvent event) {
		kafkaTemplate.send(topic, event);
	}
}
