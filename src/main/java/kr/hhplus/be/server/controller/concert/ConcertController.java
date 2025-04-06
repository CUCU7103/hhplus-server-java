package kr.hhplus.be.server.controller.concert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.config.swagger.ConcertApi;

@RestController
@RequestMapping("/api/v1/concerts")
public class ConcertController implements ConcertApi {

	/**
	 * [MOCK] 예약 가능 날짜 조회
	 *
	 */
	@GetMapping("/{concertScheduleId}/available-date")
	public ResponseEntity<ConcertSearchDateResponse> searchDate(
		@PathVariable(name = "concertScheduleId") long concertScheduleId,
		@RequestParam(name = "date") String date) {

		// 실제 서비스 호출 없이, 고정 응답을 반환합니다
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate dateTime = LocalDate.parse(date, formatter);
		ConcertScheduleInfo mockInfo = ConcertScheduleInfo.builder()
			.concertScheduleId(concertScheduleId)
			.venue("성균관대학교")
			.concertDate(dateTime)
			.build();

		ConcertSearchDateResponse concertSearchDateResponse = new ConcertSearchDateResponse("예약 날짜 조회 성공",
			mockInfo);
		return ResponseEntity.ok().body(concertSearchDateResponse);
	}

	/**
	 * [MOCK] 예약 가능 좌석 조회
	 *
	 */
	@GetMapping("/{concertScheduleId}/seats")
	public ResponseEntity<ConcertSearchSeatResponse> searchSeat(
		@PathVariable(name = "concertScheduleId") long concertScheduleId,
		@RequestParam(name = "seatId") long seatId, @RequestParam(name = "section") String section,
		@RequestParam(name = "seatNumber") long seatNumber) {

		// 실제 서비스 호출 없이, 고정 응답을 반환합니다
		SeatInfo mockInfo = SeatInfo.builder()
			.concertScheduleId(concertScheduleId)
			.seatId(seatId)
			.section(section)
			.seatNumber(seatNumber)
			.status(SeatStatus.AVAILABLE)
			.build();

		ConcertSearchSeatResponse response = new ConcertSearchSeatResponse("예약 가능한 좌석 조회 성공", mockInfo);
		return ResponseEntity.ok().body(response);
	}
}
