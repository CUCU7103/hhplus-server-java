package kr.hhplus.be.server.domain.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.concert.concert.Concert;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;

@Repository
public interface ConcertDomainRepository {

	// schedule
	Optional<ConcertSchedule> getConcertSchedule(long concertScheduleId, LocalDate localDate);

	List<ConcertSchedule> getConcertScheduleList(
		long concertId,
		LocalDate start,
		LocalDate end,
		ConcertScheduleStatus concertStatus
	);

	// concert
	Optional<Concert> findByConcertId(long concertId);

	// seat
	Page<ConcertSeat> findByConcertScheduleIdAndSeatStatusContaining(long concertScheduleId,
		ConcertSeatStatus concertSeatStatus,
		Pageable pageable);

	Optional<ConcertSeat> getConcertSeatWhere(long seatId, long concertScheduleId, LocalDate localDate,
		ConcertSeatStatus available);

	Optional<ConcertSeat> getByConcertSeatId(long seatId);

}
