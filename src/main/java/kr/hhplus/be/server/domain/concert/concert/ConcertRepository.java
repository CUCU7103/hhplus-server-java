package kr.hhplus.be.server.domain.concert.concert;

import java.util.Optional;

public interface ConcertRepository {

	Optional<Concert> findByConcertId(long concertId);
}
