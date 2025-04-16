package kr.hhplus.be.server.application.concert.info;

import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import lombok.Builder;

public record ConcertSeatInfo(long seatId, String section, long seatNumber, ConcertSeatStatus status,
							  long concertScheduleId) {
	@Builder
	public ConcertSeatInfo(long seatId, String section, long seatNumber, ConcertSeatStatus status,
		long concertScheduleId) {
		this.seatId = seatId;
		this.section = section;
		this.seatNumber = seatNumber;
		this.status = status;
		this.concertScheduleId = concertScheduleId;
	}

	public static ConcertSeatInfo from(ConcertSeat concertSeat) {
		return ConcertSeatInfo.builder()
			.seatId(concertSeat.getId())
			.section(concertSeat.getSection())
			.seatNumber(concertSeat.getSeatNumber())
			.status(concertSeat.getStatus())
			.concertScheduleId(concertSeat.getConcertSchedule().getId())
			.build();

	}
}
