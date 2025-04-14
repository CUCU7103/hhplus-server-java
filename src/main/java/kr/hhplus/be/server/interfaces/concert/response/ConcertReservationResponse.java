package kr.hhplus.be.server.interfaces.concert.response;

import kr.hhplus.be.server.domain.concert.info.ConcertReservationInfo;

public record ConcertReservationResponse(String message, ConcertReservationInfo info) {

	public static ConcertReservationResponse of(String message, ConcertReservationInfo info) {
		return new ConcertReservationResponse(message, info);
	}
}
