package kr.hhplus.be.server.controller.user;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.config.swagger.UserApi;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

	/**
	 *[MOCK] 포인트 조회 API
	 *
	 */
	@GetMapping("/{userId}/points")
	public ResponseEntity<PointResponse> getPoint(@PathVariable("userId") long userId) {
		// 실제 서비스 호출 없이, 고정 응답을 반환합니다
		UserPointInfo mockInfo = UserPointInfo.builder()
			.userId(userId)
			.point(BigDecimal.valueOf(50000))
			.build();
		PointResponse pointResponse = new PointResponse("포인트 조회 성공", mockInfo);
		return ResponseEntity.ok().body(pointResponse);
	}

	/**
	 *[MOCK] 포인트 충전 API
	 *
	 */
	@GetMapping("/{userId}/transactions")
	public ResponseEntity<PointResponse> charge(@PathVariable("userId") long userId,
		@RequestParam(name = "chargePoint") long chargePoint) {

		// 실제 서비스 호출 없이, 고정 응답을 반환합니다
		UserPointInfo mockInfo = UserPointInfo.builder()
			.userId(userId)
			.point(BigDecimal.valueOf(1000).add(BigDecimal.valueOf(chargePoint)))
			.build();
		PointResponse pointResponse = new PointResponse("포인트 충전 성공", mockInfo);
		return ResponseEntity.ok().body(pointResponse);
	}

}
