package kr.hhplus.be.server.controller.concert;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

public record ConcertScheduleInfo(long concertScheduleId, String venue,
								  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") LocalDate concertDate) {
	@Builder
	public ConcertScheduleInfo(long concertScheduleId, String venue,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") LocalDate concertDate) {
		this.concertScheduleId = concertScheduleId;
		this.venue = venue;
		this.concertDate = concertDate;
	}
}
