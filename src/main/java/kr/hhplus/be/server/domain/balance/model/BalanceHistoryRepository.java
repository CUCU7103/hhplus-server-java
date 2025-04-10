package kr.hhplus.be.server.domain.balance.model;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.balance.BalanceHistory;

@Repository
public interface BalanceHistoryRepository {
	BalanceHistory save(BalanceHistory history);
}
