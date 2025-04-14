package kr.hhplus.be.server.domain.balance;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository {
	Optional<Balance> findById(long userId);

	Optional<Balance> findByIdAndUserId(long balanceId, long userId);
}
