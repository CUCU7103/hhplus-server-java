package kr.hhplus.be.server.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.model.BalanceHistoryCommand;
import kr.hhplus.be.server.domain.balance.model.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.model.BalanceInfo;
import kr.hhplus.be.server.domain.balance.model.BalanceRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.balance.BalanceChargeRequest;
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

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Balance balance = balanceRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BALANCE));

		return BalanceInfo.builder()
			.balanceId(userId)
			.point(balance.getPoint())
			.userId(user.getId())
			.build();
	}

	// 유저의 포인트 충전 메서드
	@Transactional
	public BalanceInfo chargePoint(long userId, BalanceChargeRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		Balance balance = balanceRepository.findByIdAndUserId(request.toCommand().balanceId(), userId)
			.orElseGet(() -> Balance.of(BigDecimal.ZERO, LocalDateTime.now(), user));

		Balance delta = balance.chargePoint(request.toCommand().chargePoint());
		balanceHistoryRepository.save(BalanceHistory
			.createdHistory(new BalanceHistoryCommand(balance, delta.getPoint())));

		return BalanceInfo.builder()
			.balanceId(userId)
			.point(balance.getPoint())
			.userId(delta.getId())
			.build();
	}

}
