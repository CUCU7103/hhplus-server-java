package kr.hhplus.be.server.global.event.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;

public class PaymentCompleteEventFixture {
	public static PaymentCompletedEvent createForRankingTest(long scheduleId, String concertTitle,
		LocalDate concertDate, LocalDateTime concertOpenDate) {
		return PaymentCompletedEvent.builder()
			.scheduleId(scheduleId)
			.concertTitle(concertTitle)
			.concertDate(concertDate)
			.concertOpenDate(concertOpenDate)
			.build();
	}

	public static PaymentCompletedEvent createForMessageTest(long paymentId, long userId,
		BigDecimal money, LocalDateTime createAt) {
		return PaymentCompletedEvent.builder()
			.paymentId(paymentId)
			.userId(userId)
			.money(money)
			.createAt(createAt)
			.build();
	}

}
