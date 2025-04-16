package kr.hhplus.be.server.infrastructure.reservation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

	Optional<Reservation> getById(long reservationId);

	List<Reservation> getConcertReservationStatus(ReservationStatus status);

}
