package kr.hhplus.be.server.config.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.controller.payment.PaymentRequest;
import kr.hhplus.be.server.controller.payment.PaymentResponse;

@Tag(name = "Payment API", description = "좌석 결제 API")
public interface PaymentApi {

	@Operation(summary = "[MOCK] 좌석 결제 API", description = "좌석 결제를 진행합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "좌석 결제 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = PaymentResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "좌석 결제 성공",
						  "info": {
						    "paymentId": 1,
						    "reservationId": 1,
						    "userId": 1,
						    "price": 50000
						  }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "좌석 결제 실패",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "errorResponse",
					summary = "400 에러 응답 예시",
					value = """
						{
						  "message": "좌석 결제 실패",
						  "info": null
						}
						"""
				)
			)
		)
	})
	@PostMapping("/transaction")
	ResponseEntity<PaymentResponse> seatPayment(@RequestBody PaymentRequest dto);

}
