package kr.hhplus.be.server.infrastructure.concert.rank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistory;

@Repository
public interface ConcertRankingHistoryJpaRepository extends JpaRepository<ConcertRankingHistory, Long> {
}
