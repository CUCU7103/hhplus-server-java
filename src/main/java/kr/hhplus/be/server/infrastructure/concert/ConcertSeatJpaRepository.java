package kr.hhplus.be.server.infrastructure.concert;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;

public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {

	Page<ConcertSeat> findByConcertScheduleIdAndStatus(long concertScheduleId,
		ConcertSeatStatus concertSeatStatus,
		Pageable pageable);

	@Query("SELECT cs FROM ConcertSeat cs WHERE cs.id = :seatId AND cs.concertSchedule.id = :concertScheduleId "
		+ "AND cs.concertSchedule.concertDate = :localDate AND cs.status = :available")
	Optional<ConcertSeat> getConcertSeatWhere(
		@Param("seatId") long seatId,
		@Param("concertScheduleId") long concertScheduleId,
		@Param("localDate") LocalDate concertDate,
		@Param("available") ConcertSeatStatus available);

	long countByIdAndStatus(long concertId, ConcertSeatStatus concertSeatStatus);
}
