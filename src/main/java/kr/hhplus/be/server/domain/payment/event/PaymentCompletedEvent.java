package kr.hhplus.be.server.domain.payment.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

public record PaymentCompletedEvent(long scheduleId, String concertTitle, LocalDate concertDate,
									LocalDateTime concertOpenDate, long paymentId, long userId, BigDecimal money,
									LocalDateTime createAt) {

	@Builder
	public PaymentCompletedEvent(long scheduleId, String concertTitle, LocalDate concertDate,
		LocalDateTime concertOpenDate,
		long paymentId, long userId, BigDecimal money, LocalDateTime createAt) {
		this.scheduleId = scheduleId;
		this.concertTitle = concertTitle;
		this.concertDate = concertDate;
		this.concertOpenDate = concertOpenDate;
		this.paymentId = paymentId;
		this.userId = userId;
		this.money = money;
		this.createAt = createAt;
	}

	public static PaymentCompletedEvent create(long scheduleId, String concertTitle, LocalDate concertDate,
		LocalDateTime concertOpenDate,
		long paymentId, long userId, BigDecimal money, LocalDateTime createAt) {
		return PaymentCompletedEvent.builder().scheduleId(scheduleId)
			.concertTitle(concertTitle)
			.concertDate(concertDate)
			.concertOpenDate(concertOpenDate)
			.paymentId(paymentId)
			.userId(userId)
			.money(money)
			.createAt(createAt)
			.build();
	}

}
