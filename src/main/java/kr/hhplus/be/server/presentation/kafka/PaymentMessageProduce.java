package kr.hhplus.be.server.presentation.kafka;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.message.PaymentMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageProduce {

	private final PaymentMessageProducer messageProducer;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void sendToKafka(PaymentCompletedEvent event) {
		log.info("Kafka 전송 시작: {}", event);
		messageProducer.send(event);
		log.info("Kafka 전송 완료: {}", event);
	}

}


