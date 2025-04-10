package kr.hhplus.be.server.domain.concert.command;

import java.time.LocalDate;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ConcertDateSearchCommand(LocalDate startDate, LocalDate endDate) {

	public ConcertDateSearchCommand(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		validate();
	}

	private void validate() throws CustomException {
		if (startDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.INVALID_DATE);
		}
		if (endDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.INVALID_DATE);
		}
	}
}
