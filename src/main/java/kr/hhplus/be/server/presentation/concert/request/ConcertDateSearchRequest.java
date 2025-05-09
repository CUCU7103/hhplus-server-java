package kr.hhplus.be.server.presentation.concert.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import kr.hhplus.be.server.application.concert.command.ConcertDateSearchCommand;

public record ConcertDateSearchRequest(

	@NotNull
	@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
	String startDay,
	@NotNull
	@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
	String endDay,
	@PositiveOrZero
	int page,
	@PositiveOrZero
	int size

) {

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public ConcertDateSearchCommand toCommand() {
		LocalDate startDate = LocalDate.parse(startDay, DATE_TIME_FORMATTER);
		LocalDate endDate = LocalDate.parse(endDay, DATE_TIME_FORMATTER);
		return new ConcertDateSearchCommand(startDate, endDate, page, size);
	}

}
