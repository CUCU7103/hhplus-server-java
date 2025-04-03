package kr.hhplus.be.server.controller.reservation;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.config.swagger.ReservationApi;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController implements ReservationApi {

	/**
	 *[MOCK] 좌석 예약 API
	 *
	 */
	@PostMapping("/{seatId}")
	public ResponseEntity<ReservationResponse> reserveSeat(@PathVariable(name = "seatId") long seatId,
		@RequestParam(name = "userId") long userId) {

		ReservationInfo mockInfo = ReservationInfo.builder()
			.reservationId(1)
			.seatId(seatId)
			.price(BigDecimal.valueOf(50000))
			.status(ReservationStatus.CONFIRMED)
			.userId(userId)
			.build();

		ReservationResponse response = new ReservationResponse("좌석 예약에 성공하였습니다.", mockInfo);
		return ResponseEntity.ok().body(response);

	}

}
