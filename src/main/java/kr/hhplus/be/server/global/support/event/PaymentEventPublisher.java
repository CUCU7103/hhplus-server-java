package kr.hhplus.be.server.global.support.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class PaymentEventPublisher extends ApplicationEvent {
	private long scheduleId;

	public PaymentEventPublisher(Object source, long scheduleId) {
		super(source);
		this.scheduleId = scheduleId;
	}
}
