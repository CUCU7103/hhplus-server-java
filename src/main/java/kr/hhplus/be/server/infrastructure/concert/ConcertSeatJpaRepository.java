package kr.hhplus.be.server.infrastructure.concert;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;

public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {

	Page<ConcertSeat> findByConcertScheduleIdAndStatusContaining(long concertScheduleId,
		ConcertSeatStatus concertSeatStatus,
		Pageable pageable);

	Optional<ConcertSeat> getConcertSeatWhere(long seatId, long concertScheduleId, LocalDate localDate,
		ConcertSeatStatus available);

	Optional<ConcertSeat> getById(long seatId);

}
