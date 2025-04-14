package kr.hhplus.be.server.domain.concert.command;

import java.time.LocalDate;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ConcertReservationCommand(long concertScheduleId, LocalDate concertScheduleDate, long userId) {

	public ConcertReservationCommand(long concertScheduleId, LocalDate concertScheduleDate, long userId) {
		this.concertScheduleId = concertScheduleId;
		this.concertScheduleDate = concertScheduleDate;
		this.userId = userId;
		validate();
	}

	private void validate() {
		if (concertScheduleId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_CONCERT_SCHEDULE_ID);
		}
		if (concertScheduleDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.INVALID_DATE);
		}
		if (userId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_USER_ID);
		}
	}

}
