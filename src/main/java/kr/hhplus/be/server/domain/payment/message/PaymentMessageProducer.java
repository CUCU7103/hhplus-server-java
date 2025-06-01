package kr.hhplus.be.server.domain.payment.message;

import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;

public interface PaymentMessageProducer {

	void send(PaymentCompletedEvent event);

	void sendDlt(String topic, PaymentCompletedEvent event);

}
