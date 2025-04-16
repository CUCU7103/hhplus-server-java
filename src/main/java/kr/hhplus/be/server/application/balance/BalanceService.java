package kr.hhplus.be.server.application.balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceRepository;
import kr.hhplus.be.server.domain.balance.history.BalanceHistory;
import kr.hhplus.be.server.domain.balance.history.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BalanceService {

	private final BalanceRepository balanceRepository;
	private final BalanceHistoryRepository balanceHistoryRepository;
	private final UserRepository userRepository;

	// 유저의 포인트 조회 메서드
	@Transactional(readOnly = true)
	public BalanceInfo getPoint(long userId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Balance balance = balanceRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BALANCE));

		return BalanceInfo.from(balance.getId(), balance.getMoneyVO(), userId);
	}

	// 유저의 포인트 충전 메서드
	@Transactional
	public BalanceInfo chargePoint(long userId, ChargeBalanceCommand command) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Balance balance = balanceRepository.findByIdAndUserId(command.balanceId(), userId)
			.orElseGet(() -> Balance.of(MoneyVO.of(BigDecimal.ZERO), LocalDateTime.now(), userId));

		Balance delta = balance.chargePoint(command.chargePoint());
		balanceHistoryRepository.save(BalanceHistory
			.createdHistory(balance, delta.getMoneyVO()));

		return BalanceInfo.from(balance.getId(), delta.getMoneyVO(), userId);

	}

}
