package kr.hhplus.be.server.domain.balance.balance;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.balance.history.BalanceHistory;

@Repository
public interface BalanceRepository {
	Optional<Balance> findById(long userId);

	Optional<Balance> findByIdAndUserId(long balanceId, long userId);

	BalanceHistory save(BalanceHistory history);
}
