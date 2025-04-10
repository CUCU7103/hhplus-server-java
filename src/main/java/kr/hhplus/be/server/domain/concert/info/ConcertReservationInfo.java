package kr.hhplus.be.server.domain.concert.info;

import java.math.BigDecimal;

import kr.hhplus.be.server.domain.concert.ConcertReservation;
import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import lombok.Builder;

public record ConcertReservationInfo(long reservationId, BigDecimal price, ConcertReservationStatus status, long userId,
									 long seatId) {
	@Builder
	public ConcertReservationInfo(long reservationId, BigDecimal price, ConcertReservationStatus status, long userId,
		long seatId) {
		this.reservationId = reservationId;
		this.price = price;
		this.status = status;
		this.userId = userId;
		this.seatId = seatId;
	}

	public static ConcertReservationInfo from(ConcertReservation reservation) {
		return ConcertReservationInfo.builder()
			.reservationId(reservation.getId())
			.price(reservation.getPrice())
			.status(reservation.getConcertReservationStatus())
			.userId(reservation.getUser().getId())
			.seatId(reservation.getConcertSeat().getId())
			.build();
	}
}
