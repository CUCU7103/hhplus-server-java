package kr.hhplus.be.server.application.concert.command;

import java.time.LocalDate;

import jakarta.validation.constraints.PositiveOrZero;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ConcertSeatSearchCommand(LocalDate concertDate, int page, int size) {

	public ConcertSeatSearchCommand(LocalDate concertDate, @PositiveOrZero int page,
		@PositiveOrZero int size) {
		this.concertDate = concertDate;
		this.page = page;
		this.size = size;
		validate();
	}

	private void validate() throws CustomException {
		if (concertDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.INVALID_DATE);
		}
	}

}
