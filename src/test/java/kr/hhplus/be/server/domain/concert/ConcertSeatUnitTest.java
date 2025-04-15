package kr.hhplus.be.server.domain.concert;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;

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
		concertSeat.changeStatus(ConcertSeatStatus.HELD);
		// then
		assertThat(concertSeat.getStatus()).isEqualTo(ConcertSeatStatus.HELD);
	}

}
