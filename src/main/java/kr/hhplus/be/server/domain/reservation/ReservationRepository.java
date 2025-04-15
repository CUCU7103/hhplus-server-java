package kr.hhplus.be.server.domain.reservation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository {

	Optional<Reservation> getByConcertReservationId(long reservationId);

	Reservation save(Reservation reservation);

	List<Reservation> getConcertReservationStatus(ReservationStatus status);
}
