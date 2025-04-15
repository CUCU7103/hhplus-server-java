package kr.hhplus.be.server.domain.concert.seat;

public enum ConcertSeatStatus {
	AVAILABLE("예약가능"), HELD("임시예약"), BOOKED("예약확정");

	ConcertSeatStatus(String status) {
	}
}
