package kr.hhplus.be.server.infrastructure.balance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.balance.balance.Balance;

@Repository
public interface BalanceJpaRepository extends JpaRepository<Balance, Long> {
	Optional<Balance> findByIdAndUserId(long balanceId, long userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Balance> findByUserId(long userId);
}
