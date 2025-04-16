package kr.hhplus.be.server.infrastructure.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {

	private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
	private final ConcertSeatJpaRepository concertSeatJpaRepository;
	private final ConcertJpaRepository concertJpaRepository;

	@Override
	public Optional<ConcertSchedule> getConcertSchedule(long concertScheduleId, LocalDate localDate) {
		return concertScheduleJpaRepository.findByIdAndConcertDate(concertScheduleId, localDate);
	}

	@Override
	public List<ConcertSchedule> getConcertScheduleList(long concertId, LocalDate start, LocalDate end,
		ConcertScheduleStatus concertStatus) {
		return concertScheduleJpaRepository.findByConcertIdAndConcertDateBetweenAndStatus(concertId, start, end,
			concertStatus);
	}

	@Override
	public Optional<Concert> findByConcertId(long concertId) {
		return concertJpaRepository.findById(concertId);
	}

	@Override
	public Page<ConcertSeat> findByConcertScheduleIdAndSeatStatusContaining(long concertScheduleId,
		ConcertSeatStatus concertSeatStatus, Pageable pageable) {
		return concertSeatJpaRepository.findByConcertScheduleIdAndStatus(concertScheduleId, concertSeatStatus,
			pageable);
	}

	@Override
	public Optional<ConcertSeat> getConcertSeatWhere(long seatId, long concertScheduleId, LocalDate localDate,
		ConcertSeatStatus available) {
		return concertSeatJpaRepository.getConcertSeatWhere(seatId, concertScheduleId, localDate, available);
	}

	@Override
	public Optional<ConcertSeat> getByConcertSeatId(long seatId) {
		return concertSeatJpaRepository.findById(seatId);
	}
}
