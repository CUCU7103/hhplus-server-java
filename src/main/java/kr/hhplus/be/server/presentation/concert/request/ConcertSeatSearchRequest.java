package kr.hhplus.be.server.presentation.concert.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import kr.hhplus.be.server.application.concert.command.ConcertSeatSearchCommand;

public record ConcertSeatSearchRequest(
	@NotEmpty
	@PositiveOrZero
	long concertScheduleId,
	@NotEmpty
	@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}}")
	String date,
	@PositiveOrZero
	int page,
	@PositiveOrZero
	int size
) {

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public ConcertSeatSearchCommand toCommand() {
		LocalDate dateTime = LocalDate.parse(date, DATE_TIME_FORMATTER);
		return new ConcertSeatSearchCommand(concertScheduleId, dateTime, page, size);
	}
}
