package kr.hhplus.be.server.application.reservation;

import java.time.LocalDate;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ReservationCommand(long concertScheduleId, LocalDate concertScheduleDate) {

	public ReservationCommand(long concertScheduleId, LocalDate concertScheduleDate) {
		this.concertScheduleId = concertScheduleId;
		this.concertScheduleDate = concertScheduleDate;
		validate();
	}

	private void validate() {
		if (concertScheduleId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_CONCERT_SCHEDULE_ID);
		}
		if (concertScheduleDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.INVALID_DATE);
		}
	}

}
