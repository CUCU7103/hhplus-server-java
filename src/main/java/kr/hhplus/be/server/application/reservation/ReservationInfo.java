package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import lombok.Builder;

public record ReservationInfo(long reservationId, MoneyVO price, ReservationStatus status, long userId,
							  long seatId) {
	@Builder
	public ReservationInfo(long reservationId, MoneyVO price, ReservationStatus status, long userId,
		long seatId) {
		this.reservationId = reservationId;
		this.price = price;
		this.status = status;
		this.userId = userId;
		this.seatId = seatId;
	}

	public static ReservationInfo from(Reservation reservation) {
		return ReservationInfo.builder()
			.reservationId(reservation.getId())
			.price(reservation.getPrice())
			.status(reservation.getReservationStatus())
			.userId(reservation.getUser().getId())
			.seatId(reservation.getConcertSeat().getId())
			.build();
	}
}
