package kr.hhplus.be.server.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.MoneyVO;
import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.BalanceRepository;
import kr.hhplus.be.server.domain.balance.model.BalanceHistoryInfo;
import kr.hhplus.be.server.domain.balance.model.BalanceInfo;
import kr.hhplus.be.server.domain.balance.model.ChargeBalanceCommand;
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

		return BalanceInfo.of(balance.getId(), balance.getMoneyVO(), userId);
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
			.createdHistory(new BalanceHistoryInfo(balance.getMoneyVO(), delta.getMoneyVO(), balance)));

		return BalanceInfo.of(balance.getId(), delta.getMoneyVO(), userId);

	}

}
