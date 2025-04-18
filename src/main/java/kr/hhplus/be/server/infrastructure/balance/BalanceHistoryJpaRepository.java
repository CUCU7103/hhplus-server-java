package kr.hhplus.be.server.infrastructure.balance;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.balance.history.BalanceHistory;

public interface BalanceHistoryJpaRepository extends JpaRepository<BalanceHistory, Long> {
}
