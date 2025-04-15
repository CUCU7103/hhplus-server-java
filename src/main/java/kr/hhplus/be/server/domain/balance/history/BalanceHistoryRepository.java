package kr.hhplus.be.server.domain.balance.history;

import org.springframework.stereotype.Repository;

@Repository
public interface BalanceHistoryRepository {
	BalanceHistory save(BalanceHistory history);
}
