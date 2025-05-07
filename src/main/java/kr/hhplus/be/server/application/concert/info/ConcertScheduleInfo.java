package kr.hhplus.be.server.application.concert.info;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import lombok.Builder;

public record ConcertScheduleInfo(
	long id,
	String venue,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	LocalDate concertDate,
	long totalCount) {

	@Builder
	public ConcertScheduleInfo(long id, String venue,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") LocalDate concertDate,
		long totalCount) {
		this.id = id;
		this.venue = venue;
		this.concertDate = concertDate;
		this.totalCount = totalCount;
	}

	public static ConcertScheduleInfo from(ConcertSchedule concertSchedule) {
		return ConcertScheduleInfo.builder()
			.id(concertSchedule.getId())
			.venue(concertSchedule.getVenue())
			.concertDate(concertSchedule.getConcertDate())
			.build();

	}
}
