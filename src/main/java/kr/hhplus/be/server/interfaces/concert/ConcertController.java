package kr.hhplus.be.server.interfaces.concert;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.application.ConcertService;
import kr.hhplus.be.server.domain.concert.info.ConcertPaymentInfo;
import kr.hhplus.be.server.global.config.swagger.ConcertApi;
import kr.hhplus.be.server.interfaces.concert.request.ConcertDateSearchRequest;
import kr.hhplus.be.server.interfaces.concert.request.ConcertPaymentRequest;
import kr.hhplus.be.server.interfaces.concert.request.ConcertReservationRequest;
import kr.hhplus.be.server.interfaces.concert.request.ConcertSeatSearchRequest;
import kr.hhplus.be.server.interfaces.concert.response.ConcertDateSearchResponse;
import kr.hhplus.be.server.interfaces.concert.response.ConcertPaymentResponse;
import kr.hhplus.be.server.interfaces.concert.response.ConcertReservationResponse;
import kr.hhplus.be.server.interfaces.concert.response.ConcertSeatSearchResponse;
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
			.body(ConcertDateSearchResponse.of(concertService.searchDate(concertId, request)));
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
			.body(ConcertSeatSearchResponse.of(concertService.searchSeat(concertScheduleId, request)));
	}

	/**
	 * 좌석 예약 api
	 */
	@PostMapping("/{seatId}/seats")
	public ResponseEntity<ConcertReservationResponse> reserveSeat(@PathVariable(name = "seatId") long seatId,
		@RequestBody ConcertReservationRequest request) {

		return ResponseEntity.ok()
			.body(ConcertReservationResponse.of("좌석 예약에 성공하였습니다", concertService.reservationSeat(seatId, request)));

	}

	/**
	 *  좌석 결제 api
	 *
	 */
	@PostMapping("/{reservationId}/transaction")
	public ResponseEntity<ConcertPaymentResponse> seatPayment(@PathVariable(name = "reservationId") long reservationId,
		@RequestBody ConcertPaymentRequest dto) {

		// 실제 서비스 호출 없이, 고정 응답을 반환합니다
		// 성공적으로 좌석이 결제된 응답을 반환한다.
		ConcertPaymentInfo mockInfo = ConcertPaymentInfo.builder()
			.paymentId(dto.paymentId())
			.reservationId(reservationId)
			.userId(dto.userId())
			.amount(dto.amount())
			.build();

		ConcertPaymentResponse response = new ConcertPaymentResponse("결제 성공", mockInfo);
		return ResponseEntity.ok().body(response);
	}

}
