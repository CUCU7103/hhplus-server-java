package kr.hhplus.be.server.global.config.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.presentation.concert.request.ConcertDateSearchRequest;
import kr.hhplus.be.server.presentation.concert.request.ConcertSeatSearchRequest;
import kr.hhplus.be.server.presentation.concert.response.ConcertDateSearchResponse;
import kr.hhplus.be.server.presentation.concert.response.ConcertSeatSearchResponse;

@Tag(name = "Concert API", description = "콘서트 관련 API")
public interface ConcertApi {

	@Operation(summary = "예약 가능 날짜 조회 API", description = "예약 가능 날짜를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "예약 가능 날짜 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ConcertDateSearchResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "예약 날짜 조회 성공",
						  "info": {
						    "id": 1,
						    "venue": "성균관대학교",
						    "concertDate": "2025-04-01"
						  }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "예약 가능 날짜 조회 실패",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "errorResponse",
					summary = "400 에러 응답 예시",
					value = """
						{
						  "message": "예약 날짜 조회 실패",
						  "info": null
						}
						"""
				)
			)
		)
	})
	@PostMapping("/{concertId}/available-date")
	ResponseEntity<ConcertDateSearchResponse> searchDate(
		@Positive @PathVariable(name = "concertId") long concertId,
		long userId,
		@Valid @RequestBody ConcertDateSearchRequest request);

	@Operation(summary = "예약 가능 좌석 조회 API", description = "예약 가능 좌석을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "예약 가능 좌석 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ConcertSeatSearchResponse.class),
				examples = @ExampleObject(
					name = "successResponse",
					summary = "성공 응답 예시",
					value = """
						{
						  "message": "예약 가능 좌석 조회 성공",
						  "info": {
						    "id": 1,
						    "seatId": 1,
						    "section": "A",
						    "seatNumber" : 10
						  }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "예약 가능 날짜 조회 실패",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					name = "errorResponse",
					summary = "400 에러 응답 예시",
					value = """
						{
						  "message": "유효하지 않은 스케줄입니다",
						  "info": null
						}
						"""
				)
			)
		)
	})
	@GetMapping("/{concertScheduleId}/seats")
	ResponseEntity<ConcertSeatSearchResponse> searchSeat(
		@PathVariable(name = "concertScheduleId") long concertScheduleId,
		@RequestBody ConcertSeatSearchRequest request);

}
