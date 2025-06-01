package kr.hhplus.be.server.infrastructure.payment;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.event.PaymentEventPublisher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 이벤트 퍼블리셔를 수행하는 컴포넌트 역할 부여
// DIP를 사용해서 구현체를 갈아 낄 수 있도록 개선
@Getter
@Component
@RequiredArgsConstructor
public class SpringPaymentEventPublisher implements PaymentEventPublisher {
	private final ApplicationEventPublisher publisher;

	/**
	 * 결제 완료 시 호출할 메서드
	 */
	@Override
	public void publish(PaymentCompletedEvent completedEvent) {
		publisher.publishEvent(completedEvent);
	}
}
