package kr.hhplus.be.server.domain.reservation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.application.reservation.ReservationCommand;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public class ReservationCommandUnitTest {

	@Test
	void 유효한_입력값으로_객체_생성_성공() {
		// given
		long concertScheduleId = 1L;
		LocalDate concertScheduleDate = LocalDate.now().plusDays(7);

		// when & then
		assertThatNoException()
			.isThrownBy(() -> new ReservationCommand(concertScheduleId, concertScheduleDate));
	}

	@Test
	void 콘서트_스케줄_ID가_0이하면_예외_발생() {
		// given
		long invalidConcertScheduleId = 0L;
		LocalDate concertScheduleDate = LocalDate.now().plusDays(7);
		long userId = 1L;

		// when & then
		assertThatThrownBy(() -> new ReservationCommand(invalidConcertScheduleId, concertScheduleDate))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_CONCERT_SCHEDULE_ID.getMessage());
	}

	@Test
	void 콘서트_스케줄_ID가_음수면_예외_발생() {
		// given
		long invalidConcertScheduleId = -1L;
		LocalDate concertScheduleDate = LocalDate.now().plusDays(7);

		// when & then
		assertThatThrownBy(() -> new ReservationCommand(invalidConcertScheduleId, concertScheduleDate))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_CONCERT_SCHEDULE_ID.getMessage());
	}

	@Test
	void 콘서트_날짜가_과거면_예외_발생() {
		// given
		long concertScheduleId = 1L;
		LocalDate pastDate = LocalDate.now().minusDays(1);

		// when & then
		assertThatThrownBy(() -> new ReservationCommand(concertScheduleId, pastDate))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_DATE.getMessage());
	}

}
