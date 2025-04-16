package kr.hhplus.be.server.presentation.reservation;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.hhplus.be.server.application.reservation.ReservationCommand;

public record ReservationRequest(
	@NotNull
	@PositiveOrZero
	long concertScheduleId,
	@NotNull
	LocalDate concertScheduleDate) {

	public ReservationRequest(long concertScheduleId, LocalDate concertScheduleDate) {
		this.concertScheduleId = concertScheduleId;
		this.concertScheduleDate = concertScheduleDate;
	}

	public ReservationCommand toCommand() {
		return new ReservationCommand(concertScheduleId, concertScheduleDate);
	}

}
