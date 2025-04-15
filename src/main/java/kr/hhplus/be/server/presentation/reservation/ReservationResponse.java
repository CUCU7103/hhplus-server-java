package kr.hhplus.be.server.presentation.reservation;

import kr.hhplus.be.server.application.reservation.ReservationInfo;

public record ReservationResponse(String message, ReservationInfo info) {

	public static ReservationResponse of(String message, ReservationInfo info) {
		return new ReservationResponse(message, info);
	}
}
