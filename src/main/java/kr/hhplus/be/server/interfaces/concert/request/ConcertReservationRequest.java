package kr.hhplus.be.server.interfaces.concert.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.hhplus.be.server.domain.concert.command.ConcertReservationCommand;

public record ConcertReservationRequest(
	@NotNull
	@PositiveOrZero
	long concertScheduleId,
	@NotNull
	LocalDate concertScheduleDate,
	@NotNull
	long userId) {

	public ConcertReservationRequest(long concertScheduleId, LocalDate concertScheduleDate, long userId) {
		this.concertScheduleId = concertScheduleId;
		this.concertScheduleDate = concertScheduleDate;
		this.userId = userId;
	}

	public ConcertReservationCommand toCommand() {
		return new ConcertReservationCommand(concertScheduleId, concertScheduleDate, userId);
	}

}
