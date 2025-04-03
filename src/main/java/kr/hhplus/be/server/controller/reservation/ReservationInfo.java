package kr.hhplus.be.server.controller.reservation;

import java.math.BigDecimal;

import lombok.Builder;

public record ReservationInfo(long reservationId, BigDecimal price, ReservationStatus status, long userId,
							  long seatId) {
	@Builder
	public ReservationInfo(long reservationId, BigDecimal price, ReservationStatus status, long userId, long seatId) {
		this.reservationId = reservationId;
		this.price = price;
		this.status = status;
		this.userId = userId;
		this.seatId = seatId;
	}
}
