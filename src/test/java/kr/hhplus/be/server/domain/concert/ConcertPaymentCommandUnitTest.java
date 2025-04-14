package kr.hhplus.be.server.domain.concert;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.concert.command.ConcertPaymentCommand;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

class ConcertPaymentCommandUnitTest {

	@Test
	void 유효한_파라미터로_생성_성공() {
		// act
		ConcertPaymentCommand command = new ConcertPaymentCommand(1L, 2L, 3L, new BigDecimal("10000"));

		// assert
		assertThat(command.paymentId()).isEqualTo(1L);
		assertThat(command.userId()).isEqualTo(2L);
		assertThat(command.seatId()).isEqualTo(3L);
		assertThat(command.amount()).isEqualTo(new BigDecimal("10000"));
	}

	@Test
	void 결제ID가_0이하면_예외발생() {
		// act, assert
		assertThatThrownBy(() -> new ConcertPaymentCommand(0L, 2L, 3L, new BigDecimal("10000")))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_ID.getMessage());
	}

	@Test
	void 사용자ID가_0이하면_예외발생() {
		// act, assert
		assertThatThrownBy(() -> new ConcertPaymentCommand(1L, 0L, 3L, new BigDecimal("10000")))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_USER_ID.getMessage());
	}

	@Test
	void 좌석ID가_0이하면_예외발생() {
		// act, assert
		assertThatThrownBy(() -> new ConcertPaymentCommand(1L, 2L, 0L, new BigDecimal("10000")))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_SEAT_ID.getMessage());
	}

	@Test
	void 금액이_음수면_예외발생() {
		// act, assert
		assertThatThrownBy(() -> new ConcertPaymentCommand(1L, 2L, 3L, new BigDecimal("-1")))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_POINT.getMessage());
	}

}
