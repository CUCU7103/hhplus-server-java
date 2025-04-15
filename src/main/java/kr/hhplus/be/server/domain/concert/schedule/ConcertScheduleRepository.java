package kr.hhplus.be.server.domain.concert.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface ConcertScheduleRepository {
	Optional<ConcertSchedule> getConcertSchedule(long concertScheduleId, LocalDate localDate);

	List<ConcertSchedule> getConcertScheduleList(
		long concertId,
		LocalDate start,
		LocalDate end,
		ConcertScheduleStatus concertStatus
	);

}
