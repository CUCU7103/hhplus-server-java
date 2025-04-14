package kr.hhplus.be.server.domain.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;

@Repository
public interface ConcertRepository {

	Optional<ConcertSchedule> getConcertSchedule(long concertScheduleId, LocalDate localDate);

	List<ConcertSchedule> getConcertScheduleList(
		long concertId,
		LocalDate start,
		LocalDate end,
		ConcertScheduleStatus concertStatus
	);

	Page<ConcertSeat> findByConcertScheduleIdAndSeatStatusContaining(long concertScheduleId,
		ConcertSeatStatus concertSeatStatus,
		Pageable pageable);

	Optional<Concert> findByConcertId(long concertId);

	Optional<ConcertSeat> getConcertSeatWhere(long seatId, long concertScheduleId, LocalDate localDate,
		ConcertSeatStatus available);

	Optional<ConcertReservation> getByConcertReservationId(long reservationId);

	Optional<ConcertSeat> getByConcertSeatId(long seatId);

	ConcertReservation save(ConcertReservation reservation);

	ConcertPayment save(ConcertPayment payment);

	List<ConcertReservation> getConcertReservationStatus(ConcertReservationStatus status);
}
