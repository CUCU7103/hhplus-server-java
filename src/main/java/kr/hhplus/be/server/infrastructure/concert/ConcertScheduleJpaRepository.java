package kr.hhplus.be.server.infrastructure.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;

public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {
	Optional<ConcertSchedule> findByIdAndConcertDate(long concertScheduleId, LocalDate concertDate);

	List<ConcertSchedule> findByConcertIdAndConcertDateBetweenAndStatus(long concertId, LocalDate start, LocalDate end,
		ConcertScheduleStatus status);
}
