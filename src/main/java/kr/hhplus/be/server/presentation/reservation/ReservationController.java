package kr.hhplus.be.server.presentation.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.application.reservation.ReservationService;
import kr.hhplus.be.server.global.config.swagger.ReservationApi;
import kr.hhplus.be.server.global.support.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController implements ReservationApi {

	private final ReservationService reservationService;

	@PostMapping("/{seatId}/seats")
	public ResponseEntity<ReservationResponse> reserveSeat(@PathVariable(name = "seatId") long seatId,
		@CurrentUserId long userId,
		@RequestBody ReservationRequest request) {
		return ResponseEntity.ok()
			.body(ReservationResponse.of("좌석 예약에 성공하였습니다",
				reservationService.reserve(seatId, userId, request.toCommand())));

	}

}
