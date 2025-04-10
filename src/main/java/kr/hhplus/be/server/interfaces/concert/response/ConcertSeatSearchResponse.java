package kr.hhplus.be.server.interfaces.concert.response;

import java.util.List;

import kr.hhplus.be.server.domain.concert.info.ConcertSeatInfo;

public record ConcertSeatSearchResponse(String message, List<ConcertSeatInfo> info) {

	public ConcertSeatSearchResponse(String message, List<ConcertSeatInfo> info) {
		this.message = message;
		this.info = info;
	}

	public static ConcertSeatSearchResponse of(List<ConcertSeatInfo> info) {
		return new ConcertSeatSearchResponse("날짜 조회에 성공하였습니다.", info);
	}
}
