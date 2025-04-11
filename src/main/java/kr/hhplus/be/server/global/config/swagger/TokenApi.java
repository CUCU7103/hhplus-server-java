package kr.hhplus.be.server.global.config.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.interfaces.token.TokenResponse;
import kr.hhplus.be.server.interfaces.token.TokenSearchResponse;

@Tag(name = "Token API", description = "유저 토큰 관련 API")
public interface TokenApi {

	@Operation(summary = "[MOCK] 유저 대기열 토큰 발급 API", description = "유저 대기열에 토큰을 발급합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "토큰 발급 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = TokenResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "토큰 발급 성공",
						  "info": {
						    "tokenValue": "78f771f7-d5ff-407a-a75c-ef2bd1a56b66",
						    "status": "WAITING",
						    "createdAt": "2025-04-01",
						    "balanceId": 1
						  }
						}
						"""
				)
			)
		)
	})
	@PostMapping("/{userId}/issue")
	ResponseEntity<TokenResponse> issueToken(@PathVariable("userId") long userId);

	@Operation(summary = "[MOCK] 대기열 조회 API", description = "대기열에서 토큰 활성 상태를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "대기열 순서 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = TokenResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "대기열 순서 조회 성공",
						  "info": {
						    "tokenValue": "78f771f7-d5ff-407a-a75c-ef2bd1a56b66",
						    "status": "ACTIVE",
						    "createdAt": "2025-04-01T14:30:00",
						    "updatedAt": "2025-04-01T14:32:00",
						    "balanceId": 1
						  }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "토큰이 대기 상태가 아닙니다",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "errorResponse",
					summary = "400 에러 응답 예시",
					value = """
						{
						  "message": "토큰이 대기 상태가 아닙니다",
						  "info": null
						}
						"""
				)
			)
		)
	})
	@GetMapping("/{userId}")
	ResponseEntity<TokenSearchResponse> searchToken(@PathVariable(name = "userId") long userId);

}
