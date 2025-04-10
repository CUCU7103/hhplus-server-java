package kr.hhplus.be.server.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import kr.hhplus.be.server.domain.concert.command.ConcertDateSearchCommand;
import kr.hhplus.be.server.domain.concert.command.ConcertSeatSearchCommand;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

class ConcertCommandUnitTest {

	@Test
	void ConcertDateSearchCommand_생성시_startDate가_현재보다_이전이면_예외처리() {
		// given
		LocalDate startDate = LocalDate.now().minusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(1);

		// when & then
		assertThatThrownBy(() -> new ConcertDateSearchCommand(startDate, endDate))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_DATE.getMessage());
	}

	@Test
	void ConcertDateSearchCommand_생성시_endDate가_현재보다_이전이면_예외처리() {
		// given
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().minusDays(1);

		// when & then
		assertThatThrownBy(() -> new ConcertDateSearchCommand(startDate, endDate))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_DATE.getMessage());
	}

	@Test
	void 입력받은_날짜값이_유효하다면_ConcertDateSearchCommand_객체_생성() {
		// arrange
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(1);

		// act & assert
		assertThatNoException()
			.isThrownBy(() -> new ConcertDateSearchCommand(startDate, endDate));
	}

	@ParameterizedTest
	@ValueSource(longs = {-1L, 0L})
	void ConcertSeatSearchCommand_에서_concertScheduleId가_0_이하이면_INVALID_BALANCED_ID_예외가_발생한다(long invalidId) {
		// arrange
		LocalDate futureDate = LocalDate.now().plusDays(1);

		// act & assert
		assertThatThrownBy(() -> new ConcertSeatSearchCommand(invalidId, futureDate, 0, 10))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_BALANCED_ID.getMessage());
	}

	@Test
	void ConcertSeatSearchCommand_에서_concertDate가_현재보다_이전이면_INVALID_DATE_예외가_발생한다() {
		// arrange
		long validId = 10L;
		LocalDate pastDate = LocalDate.now().minusDays(1);

		// act & assert
		assertThatThrownBy(() -> new ConcertSeatSearchCommand(validId, pastDate, 0, 10))
			.isInstanceOf(CustomException.class)
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_DATE.getMessage());
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -5})
	void ConcertSeatSearchCommand_에서_size가_0_이하일_경우_기본값으로_설정된다(int invalidSize) {
		// arrange
		long validId = 10L;
		LocalDate futureDate = LocalDate.now().plusDays(1);

		// act
		ConcertSeatSearchCommand command =
			new ConcertSeatSearchCommand(validId, futureDate, 0, invalidSize);

		// assert
		assertThat(command.size()).isEqualTo(10);
	}

	@Test
	void 모든_값이_정상적이면_예외_없이_ConcertSeatSearchCommand_객체가_생성된다() {
		// arrange
		long validId = 10L;
		LocalDate futureDate = LocalDate.now().plusDays(1);
		int page = 1;
		int size = 5;

		// act
		ConcertSeatSearchCommand command = new ConcertSeatSearchCommand(validId, futureDate, page, size);

		// assert
		assertThat(command.concertScheduleId()).isEqualTo(validId);
		assertThat(command.concertDate()).isEqualTo(futureDate);
		assertThat(command.page()).isEqualTo(page);
		assertThat(command.size()).isEqualTo(size);
	}

}
