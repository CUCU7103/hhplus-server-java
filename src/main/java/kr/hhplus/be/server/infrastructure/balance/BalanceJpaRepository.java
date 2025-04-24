package kr.hhplus.be.server.infrastructure.balance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.balance.balance.Balance;

@Repository
public interface BalanceJpaRepository extends JpaRepository<Balance, Long> {

	Optional<Balance> findByIdAndUserId(long balanceId, long userId);

	Optional<Balance> findByUserId(long userId);
}
