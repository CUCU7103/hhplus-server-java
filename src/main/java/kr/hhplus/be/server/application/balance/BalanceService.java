package kr.hhplus.be.server.application.balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import kr.hhplus.be.server.global.support.lock.model.LockType;
import kr.hhplus.be.server.global.support.lock.model.WithLock;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BalanceService {

	private final BalanceRepository balanceRepository;
	private final UserRepository userRepository;

	// 유저의 포인트 조회 메서드
	@Transactional(readOnly = true)
	public BalanceInfo getPoint(long userId) {
		userRepository.findById(userId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Balance balance = balanceRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BALANCE));

		return BalanceInfo.from(balance.getId(), balance.getMoneyVO(), userId);
	}

	// 유저의 포인트 충전 메서드
	@WithLock(
		key = "balance:charge",           // 락 키: balance:charge:123 과 같이 생성
		type = LockType.REDIS_SIMPLE,                   // Redis Spin Lock 사용
		expireMillis = 5000
	)
	@Transactional
	public BalanceInfo chargePoint(long userId, ChargeBalanceCommand command) {
		userRepository.findById(userId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
		try {
			Balance balance = balanceRepository.findByIdAndUserId(command.balanceId(), userId)
				.orElseGet(() -> Balance.create(MoneyVO.create(BigDecimal.ZERO), LocalDateTime.now(), userId));
			MoneyVO previousPoint = balance.getMoneyVO();
			Balance delta = balance.chargePoint(command.chargePoint());
			balanceRepository.saveAndFlush(BalanceHistory.createdHistory(delta, previousPoint));
			return BalanceInfo.from(balance.getId(), delta.getMoneyVO(), userId);
		} catch (OptimisticLockException e) {
			throw new CustomException(CustomErrorCode.CHARGED_ERROR);
		} catch (CustomException e) {
			throw e;
		}

	}

}
