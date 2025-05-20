package kr.hhplus.be.server.infrastructure.rank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.rank.RankingHistory;

@Repository
public interface RankingHistoryJpaRepository extends JpaRepository<RankingHistory, Long> {
}
