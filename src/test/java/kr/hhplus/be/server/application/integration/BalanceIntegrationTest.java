package kr.hhplus.be.server.application.integration;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.balance.BalanceInfo;
import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.application.balance.ChargeBalanceCommand;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class BalanceIntegrationTest {

	@Autowired
	private BalanceService balanceService;

	@Autowired
	private BalanceJpaRepository balanceJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Test
	void 예외가_발생하지_않으면_유저의_포인트_조회에_성공한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("철수").build());
		Balance balance = balanceJpaRepository.save(
			Balance.of(MoneyVO.of(BigDecimal.valueOf(1000)), LocalDateTime.now(), user.getId()));

		BalanceInfo balanceInfo = balanceService.getPoint(user.getId());
		// assert
		assertThat(balanceInfo.userId()).isEqualTo(user.getId());
		assertThat(balanceInfo.balanceId()).isEqualTo(balance.getId());
		assertThat(balanceInfo.moneyVO().getAmount().compareTo(balance.getMoneyVO().getAmount())).isZero();
	}

	@Test
	void 예외가_발생하지_않으면_유저의_포인트_충전에_성공한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("철수").build());
		Balance balance = balanceJpaRepository.save(
			Balance.of(MoneyVO.of(BigDecimal.valueOf(1000)), LocalDateTime.now(), user.getId()));
		// act
		ChargeBalanceCommand command = new ChargeBalanceCommand(balance.getId(), BigDecimal.valueOf(1000));
		BalanceInfo balanceInfo = balanceService.chargePoint(user.getId(), command);
		// assert
		assertThat(balanceInfo.userId()).isEqualTo(user.getId());
		assertThat(balanceInfo.balanceId()).isEqualTo(balance.getId());
		assertThat(balanceInfo.moneyVO()
			.getAmount()
			.compareTo(balance.getMoneyVO().getAmount().add(BigDecimal.valueOf(1000)))).isZero();
	}

}
