package kr.hhplus.be.server.domain.concert.info;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import lombok.Builder;

public record ConcertScheduleInfo(
	long id,
	String venue,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	LocalDate concertDate) {

	@Builder
	public ConcertScheduleInfo(long id, String venue,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") LocalDate concertDate) {
		this.id = id;
		this.venue = venue;
		this.concertDate = concertDate;
	}

	public static ConcertScheduleInfo from(ConcertSchedule concertSchedule) {
		return ConcertScheduleInfo.builder()
			.id(concertSchedule.getId())
			.venue(concertSchedule.getVenue())
			.concertDate(concertSchedule.getConcertDate())
			.build();

	}
}
