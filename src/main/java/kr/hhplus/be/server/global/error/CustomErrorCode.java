package kr.hhplus.be.server.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum CustomErrorCode {

	INVALID_USER_ID(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 유저 아이디"),
	INVALID_CONCERT_SCHEDULE_ID(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 콘서트 스케줄 아이디"),
	INVALID_BALANCED_ID(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 잔액 아이디 입니다"),

	INVALID_POINT(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 포인트"),
	INVALID_DATE(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 날짜를 입력하였습니다"),
	INVALID_PAGING(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 페이징 정보입니다"),
	INVALID_RESERVATION_CONCERT_SEAT(HttpStatus.BAD_REQUEST, "400", "예약 불가능한 좌석입니다"),
	INVALID_PAYMENT_ID(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 결제 아이디"),
	INVALID_RESERVATION_ID(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 예약 아이디"),
	INVALID_SEAT_ID(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 좌석 아이디"),
	INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 결제 금액입니다"),
	INVALID_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "500", "유효한 상태가 아닙니다"),

	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "404", "사용자를 찾을 수 없습니다"),
	NOT_FOUND_SCHEDULE(HttpStatus.BAD_REQUEST, "400", "해당하는 스케줄을 찾을 수 없습니다"),
	NOT_FOUND_BALANCE(HttpStatus.NOT_FOUND, "404", "포인트 조회에 실패하였습니다"),
	NOT_FOUND_CONCERT(HttpStatus.NOT_FOUND, "404", "해당하는 콘서트를 찾을 수 없습니다"),
	NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "404", "예약 정보를 찾을 수 없습니다"),
	NOT_FOUND_CONCERT_SEAT(HttpStatus.NOT_FOUND, "404", "좌석 정보를 찾을 수 없습니다"),
	NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, "404", "토큰을 찾을 수 없습니다"),

	OVER_CHARGED_POINT(HttpStatus.BAD_REQUEST, "400", "최대 충전 포인트를 초과함"),
	OVER_USED_POINT(HttpStatus.BAD_REQUEST, "400", "보유금액을 초과하여 사용함"),
	EMPTY_FIELD(HttpStatus.INTERNAL_SERVER_ERROR, " 500", "객체 생성시 누락된 필드가 있습니다"),
	NOT_HELD_RESERVATION(HttpStatus.INTERNAL_SERVER_ERROR, "500", "임시 예약된 상태가 아닙니다"),
	TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "400", "이미 만료된 토큰입니다"),
	FAILED_RESERVATION_SEAT(HttpStatus.BAD_REQUEST, "400", "좌석 예약에 실패하였습니다"),
	SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "예상하지 못한 오류가 발생하였습니다."),
	CHARGED_ERROR(HttpStatus.BAD_REQUEST, "400", "여러 번의 충전 요청이 발생하였습니다"),
	PAYMENT_ERROR(HttpStatus.BAD_REQUEST, "400", "결제에 실패 했습니다"),
	BEFORE_DATE(HttpStatus.BAD_REQUEST, "400", "현재 날짜 보다 이전 날짜를 입력하여 오류가 발생하였습니다"),
	NOT_FOUND_HEADER(HttpStatus.BAD_REQUEST, "400", "헤더를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	CustomErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
