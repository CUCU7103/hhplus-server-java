package kr.hhplus.be.server.domain.concert.info;

import java.math.BigDecimal;

import kr.hhplus.be.server.domain.concert.ConcertPayment;
import lombok.Builder;

public record ConcertPaymentInfo(long paymentId, long userId, long reservationId, BigDecimal price) {
	@Builder
	public ConcertPaymentInfo(long paymentId, long userId, long reservationId, BigDecimal price) {
		this.paymentId = paymentId;
		this.userId = userId;
		this.reservationId = reservationId;
		this.price = price;
	}

	public static ConcertPaymentInfo from(ConcertPayment payment) {
		return ConcertPaymentInfo.builder()
			.paymentId(payment.getId())
			.price(payment.getPrice())
			.reservationId(payment.getReservation().getId())
			.userId(payment.getUser().getId())
			.build();
	}
}
