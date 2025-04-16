package kr.hhplus.be.server.global.config.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.hhplus.be.server.global.support.resolver.CurrentUserId;
import kr.hhplus.be.server.presentation.reservation.ReservationRequest;
import kr.hhplus.be.server.presentation.reservation.ReservationResponse;

public interface ReservationApi {
	@Operation(summary = "[MOCK] 좌석 예약 API", description = "좌석 예약을 진행합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "좌석 예약 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ReservationResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "좌석 예약 성공",
						  "info": {
						    "reservationId": 1,
						    "price": "50000",
						    "status": "CONFIRMED",
						    "balanceId": 1,
						    "seatId": 1
						  }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "좌석 예약 실패",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "errorResponse",
					summary = "400 에러 응답 예시",
					value = """
						{
						  "message": "좌석 예약 실패",
						  "info": null
						}
						"""
				)
			)
		)
	})
	@PostMapping("/{seatId}/seats")
	ResponseEntity<ReservationResponse> reserveSeat(@PathVariable(name = "seatId") long seatId,
		@CurrentUserId long userId,
		@RequestBody ReservationRequest request);
}
