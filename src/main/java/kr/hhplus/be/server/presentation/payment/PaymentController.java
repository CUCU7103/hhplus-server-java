package kr.hhplus.be.server.presentation.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.application.payment.PaymentService;
import kr.hhplus.be.server.global.support.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/{reservationId}/transaction")
	public ResponseEntity<PaymentResponse> seatPayment(@PathVariable(name = "reservationId") long reservationId,
		@CurrentUserId long userId,
		@RequestBody PaymentRequest request) {
		return ResponseEntity.ok()
			.body(
				PaymentResponse.of("좌석 예약 성공", paymentService.payment(reservationId, userId, request.toCommand())));
	}

}
