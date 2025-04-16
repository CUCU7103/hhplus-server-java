package kr.hhplus.be.server.domain.concert.seat;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertSeatRepository {

	Page<ConcertSeat> findByConcertScheduleIdAndSeatStatusContaining(long concertScheduleId,
		ConcertSeatStatus concertSeatStatus,
		Pageable pageable);

	Optional<ConcertSeat> getConcertSeatWhere(long seatId, long concertScheduleId, LocalDate localDate,
		ConcertSeatStatus available);

	Optional<ConcertSeat> getByConcertSeatId(long seatId);
}
