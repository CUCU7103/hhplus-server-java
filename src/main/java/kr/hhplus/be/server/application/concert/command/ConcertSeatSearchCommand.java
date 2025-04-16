package kr.hhplus.be.server.application.concert.command;

import java.time.LocalDate;

import jakarta.validation.constraints.PositiveOrZero;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ConcertSeatSearchCommand(long concertScheduleId, LocalDate concertDate, int page, int size) {

	public ConcertSeatSearchCommand(long concertScheduleId, LocalDate concertDate, @PositiveOrZero int page,
		@PositiveOrZero int size) {
		this.concertScheduleId = concertScheduleId;
		this.concertDate = concertDate;
		this.page = page;
		this.size = (size > 0) ? size : 10;
		validate();
	}

	private void validate() throws CustomException {
		if (concertScheduleId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_CONCERT_SCHEDULE_ID);
		}
		if (concertDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.INVALID_DATE);
		}
	}

}
