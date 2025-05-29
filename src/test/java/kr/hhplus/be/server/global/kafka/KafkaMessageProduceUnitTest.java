package kr.hhplus.be.server.global.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.message.PaymentMessageProducer;
import kr.hhplus.be.server.global.event.model.PaymentCompleteEventFixture;
import kr.hhplus.be.server.presentation.kafka.PaymentMessageProduce;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
public class KafkaMessageProduceUnitTest {

	@Mock
	private PaymentMessageProducer paymentMessageProducer;

	@InjectMocks
	private PaymentMessageProduce paymentMessageProduce;

	@Test
	void 결제_후_카프카_메시지_전송_이벤트_로그_검증(CapturedOutput output) throws InterruptedException {
		// arrange
		PaymentCompletedEvent paymentCompletedEvent = PaymentCompleteEventFixture.createForMessageTest(1L, 1L,
			BigDecimal.valueOf(1000), LocalDateTime.now());
		// act
		// messageProducer.send() 메서드가 호출될 때 아무것도 하지 않도록 설정
		doNothing().when(paymentMessageProducer).send(any(PaymentCompletedEvent.class));

		paymentMessageProduce.sendToKafka(paymentCompletedEvent);
		// assert
		String logs = output.getOut();
		String expectedEventString = paymentCompletedEvent.toString();

		assertThat(logs)
			.contains("Kafka 전송 시작: " + expectedEventString)
			.contains("Kafka 전송 완료: " + expectedEventString);

		// messageProducer.send 메서드가 정확히 한 번 호출되었는지 검증
		verify(paymentMessageProducer, times(1)).send(paymentCompletedEvent);

	}
}
