package kr.hhplus.be.server.controller.concert;

import lombok.Builder;

public record ConcertSearchDateResponse(String message, ConcertScheduleInfo info) {
	@Builder
	public ConcertSearchDateResponse(String message, ConcertScheduleInfo info) {
		this.message = message;
		this.info = info;
	}
}
