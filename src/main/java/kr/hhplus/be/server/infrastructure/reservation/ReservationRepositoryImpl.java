package kr.hhplus.be.server.infrastructure.reservation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

	private final ReservationJpaRepository reservationJpaRepository;

	@Override
	public Optional<Reservation> getByConcertReservationId(long reservationId) {
		return reservationJpaRepository.getById(reservationId);
	}

	@Override
	public Reservation save(Reservation reservation) {
		return reservationJpaRepository.save(reservation);
	}

	@Override
	public List<Reservation> getConcertReservationStatus(ReservationStatus status) {
		return reservationJpaRepository.findByReservationStatus(status);
	}
}
