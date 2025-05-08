package kr.hhplus.be.server.infrastructure.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;

public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {
	Optional<ConcertSchedule> findByIdAndConcertDate(long concertScheduleId, LocalDate concertDate);

	List<ConcertSchedule> findByConcertIdAndConcertDateBetweenAndStatus(long concertId, LocalDate start, LocalDate end,
		ConcertScheduleStatus status);

	@Query("SELECT cs FROM ConcertSchedule cs " +
		"WHERE cs.concert.id = :concertId " +
		"AND cs.concertDate BETWEEN :start AND :end " +
		"AND cs.status = :status")
	Page<ConcertSchedule> getConcertSchedules(
		@Param("concertId") long concertId,
		@Param("start") LocalDate start,
		@Param("end") LocalDate end,
		@Param("status") ConcertScheduleStatus status,
		Pageable pageable);

	List<ConcertSchedule> findByConcertIdAndConcertDateBetweenAndStatus(long concertId, LocalDate start, LocalDate end,
		ConcertScheduleStatus status, Sort sort);

}
