package kr.hhplus.be.server.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

	Optional<Reservation> getByConcertReservationId(long reservationId);

	Reservation save(Reservation reservation);

	List<Reservation> getConcertReservationStatus(ReservationStatus status);

	Reservation saveAndFlush(Reservation reservation);
}
