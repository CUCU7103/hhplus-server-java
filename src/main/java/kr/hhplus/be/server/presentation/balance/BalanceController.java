package kr.hhplus.be.server.presentation.balance;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.global.support.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
@Validated
public class BalanceController {

	private final BalanceService balanceService;

	/**
	 * 포인트 조회 API
	 */
	@GetMapping
	public ResponseEntity<BalanceResponse> getPoint(@CurrentUserId long userId) {
		return ResponseEntity.ok().body(BalanceResponse.of("포인트 조회 성공", balanceService.getPoint(userId)));
		// 상위 -> 하위
	}

	/**
	 * 포인트 충전 API
	 */
	@PutMapping("/transactions")
	public ResponseEntity<BalanceResponse> charge(
		@CurrentUserId long userId,
		@Valid @RequestBody BalanceChargeRequest request) {
		// 실제 서비스 호출 없이, 고정 응답을 반환합니다
		return ResponseEntity.ok()
			.body(BalanceResponse.of("포인트 충전 성공", balanceService.chargePoint(userId, request.toCommand())));
	}

}
