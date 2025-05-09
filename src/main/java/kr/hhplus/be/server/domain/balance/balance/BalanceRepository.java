package kr.hhplus.be.server.domain.balance.balance;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.balance.history.BalanceHistory;

public interface BalanceRepository {
	Optional<Balance> findByUserId(long userId);

	@Lock(LockModeType.OPTIMISTIC)
	Optional<Balance> findByIdAndUserId(long balanceId, long userId);

	BalanceHistory save(BalanceHistory history);

	BalanceHistory saveAndFlush(BalanceHistory history);
}
