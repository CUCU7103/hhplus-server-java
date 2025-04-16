package kr.hhplus.be.server.presentation.concert;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.global.config.swagger.ConcertApi;
import kr.hhplus.be.server.presentation.concert.request.ConcertDateSearchRequest;
import kr.hhplus.be.server.presentation.concert.request.ConcertSeatSearchRequest;
import kr.hhplus.be.server.presentation.concert.response.ConcertDateSearchResponse;
import kr.hhplus.be.server.presentation.concert.response.ConcertSeatSearchResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/concerts")
@RequiredArgsConstructor
@Validated
public class ConcertController implements ConcertApi {
	private final ConcertService concertService;

	/**
	 *  예약 가능 날짜 조회
	 */
	@GetMapping("/{concertId}/available-date")
	public ResponseEntity<ConcertDateSearchResponse> searchDate(
		@Positive @PathVariable(name = "concertId") long concertId,
		@RequestBody ConcertDateSearchRequest request) {
		return ResponseEntity.ok()
			.body(ConcertDateSearchResponse.of(concertService.searchDate(concertId, request.toCommand())));
	}

	/**
	 *  예약 가능 좌석 조회
	 *
	 */
	@GetMapping("/{concertScheduleId}/seats")
	public ResponseEntity<ConcertSeatSearchResponse> searchSeat(
		@PathVariable(name = "concertScheduleId") long concertScheduleId,
		@RequestBody ConcertSeatSearchRequest request) {
		return ResponseEntity.ok()
			.body(ConcertSeatSearchResponse.of(concertService.searchSeat(concertScheduleId, request.toCommand())));
	}

	/**
	 * 좌석 예약 api
	 */

	/**
	 *  좌석 결제 api
	 *
	 */

}
