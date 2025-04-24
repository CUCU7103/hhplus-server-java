package kr.hhplus.be.server.application.balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceRepository;
import kr.hhplus.be.server.domain.balance.history.BalanceHistory;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BalanceService {
	private final ApplicationEventPublisher eventPublisher;
	private final BalanceRepository balanceRepository;
	private final UserRepository userRepository;

	// 유저의 포인트 조회 메서드
	@Transactional(readOnly = true)
	public BalanceInfo getPoint(long userId) {
		userRepository.findById(userId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Balance balance = balanceRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BALANCE));

		return BalanceInfo.from(balance.getId(), balance.getMoneyVO(), userId);
	}

	// 유저의 포인트 충전 메서드
	@Transactional
	public BalanceInfo chargePoint(long userId, ChargeBalanceCommand command) {
		userRepository.findById(userId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
		try {
			Balance balance = balanceRepository.findByIdAndUserId(command.balanceId(), userId)
				.orElseGet(() -> Balance.create(MoneyVO.create(BigDecimal.ZERO), LocalDateTime.now(), userId));
			MoneyVO previousPoint = balance.getMoneyVO();
			Balance delta = balance.chargePoint(command.chargePoint());
			balanceRepository.save(BalanceHistory.createdHistory(delta, previousPoint));
			return BalanceInfo.from(balance.getId(), delta.getMoneyVO(), userId);
		} catch (OptimisticLockException e) {
			throw new CustomException(CustomErrorCode.CHARGED_ERROR);
		} catch (CustomException e) {
			throw e;
		}

	}

}
