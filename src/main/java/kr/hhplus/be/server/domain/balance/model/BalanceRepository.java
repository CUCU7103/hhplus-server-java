package kr.hhplus.be.server.domain.balance.model;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.balance.Balance;

@Repository
public interface BalanceRepository {
	Optional<Balance> findById(long userId);

	Optional<Balance> findByIdAndUserId(long balanceId, long userId);
}
