package kr.hhplus.be.server.domain.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;

public interface ConcertRepository {

	// schedule
	Optional<ConcertSchedule> getConcertScheduleWithDate(long concertScheduleId, LocalDate localDate);

	List<ConcertSchedule> getConcertScheduleListOrderByDate(
		long concertId,
		LocalDate start,
		LocalDate end,
		ConcertScheduleStatus concertStatus,
		Sort sort
	);

	Optional<ConcertSchedule> findConcertSchedule(long concertScheduleId);

	// concert
	Optional<Concert> findByConcertId(long concertId);

	// seat
	Page<ConcertSeat> findByConcertScheduleIdAndSeatStatusContaining(long concertScheduleId,
		ConcertSeatStatus concertSeatStatus,
		Pageable pageable);

	Optional<ConcertSeat> getConcertSeatWhere(long seatId, long concertScheduleId, LocalDate localDate,
		ConcertSeatStatus available);

	Optional<ConcertSeat> getByConcertSeatId(long seatId);

	long getAvailableConcertSeat(long concertScheduleId);
}
