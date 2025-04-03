package kr.hhplus.be.server.controller.concert;

import lombok.Builder;

public record SeatInfo(long seatId, String section, long seatNumber, SeatStatus status, long concertScheduleId) {
	@Builder
	public SeatInfo(long seatId, String section, long seatNumber, SeatStatus status, long concertScheduleId) {
		this.seatId = seatId;
		this.section = section;
		this.seatNumber = seatNumber;
		this.status = status;
		this.concertScheduleId = concertScheduleId;
	}
}
