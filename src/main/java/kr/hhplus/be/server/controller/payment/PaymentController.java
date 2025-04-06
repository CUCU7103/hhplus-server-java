package kr.hhplus.be.server.controller.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.config.swagger.PaymentApi;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController implements PaymentApi {

	@PostMapping("/transaction")
	public ResponseEntity<PaymentResponse> seatPayment(@RequestBody PaymentRequest dto) {

		// 실제 서비스 호출 없이, 고정 응답을 반환합니다
		// 성공적으로 좌석이 결제된 응답을 반환한다.
		PaymentInfo mockInfo = PaymentInfo.builder()
			.paymentId(dto.paymentId())
			.reservationId(dto.reservationId())
			.userId(dto.userId())
			.price(dto.amount())
			.build();

		PaymentResponse response = new PaymentResponse("결제 성공", mockInfo);
		return ResponseEntity.ok().body(response);
	}
}
