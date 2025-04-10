package kr.hhplus.be.server.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;

public class ConcertSeatUnitTest {

	@Test
	void 좌석_상태_변경_성공() {
		// given
		ConcertSeat concertSeat = ConcertSeat.builder()
			.id(1L)
			.section("A")
			.seatNumber(1)
			.status(ConcertSeatStatus.AVAILABLE)
			.build();

		// when
		ConcertSeat updatedSeat = concertSeat.changeStatus(ConcertSeatStatus.HELD);

		// then
		assertThat(updatedSeat).isEqualTo(concertSeat); // 동일 객체 참조 확인
		assertThat(updatedSeat.getStatus()).isEqualTo(ConcertSeatStatus.HELD);
	}

}
