package kr.hhplus.be.server.domain.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MessageContext(long paymentId, long userId, BigDecimal money,
							 LocalDateTime createAt) {

	public MessageContext(long paymentId, long userId, BigDecimal money, LocalDateTime createAt) {
		this.paymentId = paymentId;
		this.userId = userId;
		this.money = money;
		this.createAt = createAt;
	}

	public static MessageContext of(PaymentCompletedEvent completedEvent) {
		return new MessageContext(completedEvent.paymentId(), completedEvent.userId(), completedEvent.money(),
			completedEvent.createAt());
	}

}
