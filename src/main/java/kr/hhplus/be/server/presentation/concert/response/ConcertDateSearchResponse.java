package kr.hhplus.be.server.presentation.concert.response;

import java.util.List;

import kr.hhplus.be.server.application.concert.info.ConcertScheduleInfo;
import lombok.Builder;

public record ConcertDateSearchResponse(String message, List<ConcertScheduleInfo> info) {
	@Builder
	public ConcertDateSearchResponse(String message, List<ConcertScheduleInfo> info) {
		this.message = message;
		this.info = info;
	}

	public static ConcertDateSearchResponse of(List<ConcertScheduleInfo> info) {
		return new ConcertDateSearchResponse("날짜 조회에 성공하였습니다.", info);
	}
}
