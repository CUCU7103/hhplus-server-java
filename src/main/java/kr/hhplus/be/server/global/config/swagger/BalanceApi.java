package kr.hhplus.be.server.global.config.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.presentation.balance.BalanceChargeRequest;
import kr.hhplus.be.server.presentation.balance.BalanceResponse;

@Tag(name = "Balance API", description = "잔액 관련 API")
public interface BalanceApi {

	@Operation(summary = "[MOCK] 유저의 포인트 조회 API", description = "유저의 포인트를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "유저 포인트 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BalanceResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "유저 포인트 조회 성공",
						  "info": {
						    "balanceId": 1,
						    "deltaPoint": 50000
						  }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "유저 포인트 조회 실패",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "errorResponse",
					summary = "400 에러 응답 예시",
					value = """
						{
						  "message": "유저 포인트 조회 실패",
						  "info": null
						}
						"""
				)
			)
		)
	})
	@GetMapping
	ResponseEntity<BalanceResponse> getPoint(long userId);

	@Operation(summary = "[MOCK] 유저의 포인트 충전 API", description = "유저의 포인트 충전을 진행")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "유저의 포인트 충전을 진행",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BalanceResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "유저 포인트 충전 성공",
						  "info": {
						    "balanceId": 1,
						    "deltaPoint": 51000
						  }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "유저 포인트 충전 실패",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "errorResponse",
					summary = "400 에러 응답 예시",
					value = """
						{
						  "message": "유저 포인트 충전 실패",
						  "info": null
						}
						"""
				)
			)
		)
	})
	@PutMapping("/transactions")
	ResponseEntity<BalanceResponse> charge(long userId,
		@RequestBody BalanceChargeRequest request);

}
