package kr.hhplus.be.server.infrastructure.balance;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceRepository;
import kr.hhplus.be.server.domain.balance.history.BalanceHistory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BalanceRepositoryImpl implements BalanceRepository {

	private final BalanceJpaRepository balanceJpaRepository;
	private final BalanceHistoryJpaRepository balanceHistoryJpaRepository;

	@Override
	public BalanceHistory save(BalanceHistory history) {
		return balanceHistoryJpaRepository.save(history);
	}

	@Override
	public Optional<Balance> findByUserId(long userId) {
		return balanceJpaRepository.findByUserId(userId);
	}

	@Override
	public Optional<Balance> findByIdAndUserId(long balanceId, long userId) {
		return balanceJpaRepository.findByIdAndUserId(balanceId, userId);
	}

	@Override
	public BalanceHistory saveAndFlush(BalanceHistory history) {
		return balanceHistoryJpaRepository.saveAndFlush(history);
	}
}
