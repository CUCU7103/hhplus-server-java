package kr.hhplus.be.server.application.concert.command;

import java.time.LocalDate;

import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public record ConcertDateSearchCommand(LocalDate startDate, LocalDate endDate, int page, int size) {

	public ConcertDateSearchCommand(LocalDate startDate, LocalDate endDate, int page, int size) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.page = page;
		this.size = size;
		validate();
	}

	private void validate() throws CustomException {
		if (startDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.BEFORE_DATE);
		}
		if (endDate.isBefore(LocalDate.now())) {
			throw new CustomException(CustomErrorCode.BEFORE_DATE);
		}
	}
}
