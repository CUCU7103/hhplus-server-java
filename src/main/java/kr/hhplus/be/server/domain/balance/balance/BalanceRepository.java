package kr.hhplus.be.server.domain.balance.balance;

import java.util.Optional;

import kr.hhplus.be.server.domain.balance.history.BalanceHistory;

public interface BalanceRepository {
	Optional<Balance> findById(long userId);

	Optional<Balance> findByIdAndUserId(long balanceId, long userId);

	BalanceHistory save(BalanceHistory history);
}
