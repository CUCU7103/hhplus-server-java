package kr.hhplus.be.server.domain.balance;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.MoneyVO;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

class BalanceUnitTest {

	@Test
	void 최대포인트를_초과하면_포인트_충전에_실패한다() {
		// arrange
		long balanceId = 1L;
		BigDecimal chargePoint = BigDecimal.valueOf(100000);
		Balance balance = Balance.builder()
			.id(balanceId)
			.moneyVO(MoneyVO.of(BigDecimal.valueOf(100000)))
			.build();

		// act & assert
		assertThatThrownBy(() -> balance.chargePoint(chargePoint)).isInstanceOf(
				CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_CHARGED_POINT.getMessage());
	}

	@Test
	void 최대포인트를_초과하지_않는다면_포인트_충전에_성공한다() {
		// arrange
		long balanceId = 1L;
		BigDecimal chargePoint = BigDecimal.valueOf(9000);
		Balance balance = Balance.builder()
			.id(balanceId)
			.moneyVO(MoneyVO.of(BigDecimal.valueOf(1000)))
			.build();
		//act
		Balance result = balance.chargePoint(chargePoint);

		// assert
		assertThat(result).isNotNull();
		assertThat(result.getMoneyVO().getAmount()).isEqualTo(BigDecimal.valueOf(10000));
	}

	@Test
	void 새로운_밸런스를_객체를_생성하며_유저와_연결한다() {
		// given
		long userId = 1L;
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(100000));

		//act
		Balance createBalance = Balance.of(moneyVO, LocalDateTime.now(), userId);

		// assert
		assertThat(createBalance.getCreatedAt()).isNotNull();
		assertThat(createBalance.getMoneyVO().getAmount()).isEqualTo(moneyVO.getAmount());
	}

	@Test
	void 보유한_포인트_이상_사용시_예외처리() {
		// arrange
		long balanceId = 1L;
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(10000));
		BigDecimal usePoint = BigDecimal.valueOf(100000);

		Balance balance = Balance.of(moneyVO, LocalDateTime.now(), balanceId);

		// assert
		assertThatThrownBy(() -> balance.usePoint(usePoint)).isInstanceOf(
				CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_USED_POINT.getMessage());
	}

	@Test
	void 결제금액이_보유한_포인트_이하라면_사용가능() {
		// arrange
		long balanceId = 1L;
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(10000));
		BigDecimal usePoint = BigDecimal.valueOf(5000);

		Balance balance = Balance.of(moneyVO, LocalDateTime.now(), balanceId);

		Balance result = balance.usePoint(usePoint);
		// assert
		assertThat(result).isNotNull();
		assertThat(result.getMoneyVO().getAmount()).isEqualTo(BigDecimal.valueOf(5000));
	}

	@Test
	void pointVO가_null일때_예외발생() {
		// arrange
		long userId = 1L;
		MoneyVO moneyVO = null;

		// act & assert
		assertThatThrownBy(() -> Balance.of(moneyVO, LocalDateTime.now(), userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_POINT.getMessage());
	}

	@Test
	void createdAt이_null이면_현재시간으로_설정된다() {
		// arrange
		long userId = 1L;
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(1000));

		// act
		Balance balance = Balance.of(moneyVO, LocalDateTime.now(), userId);

		// assert
		assertThat(balance.getCreatedAt()).isNotNull();
		assertThat(balance.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
	}

	@Test
	void userId가_0이면_예외발생() {
		// arrange
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(1000));

		// act & assert
		assertThatThrownBy(() -> Balance.of(moneyVO, LocalDateTime.now(), 0))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_USER_ID.getMessage());
	}

	@Test
	void 모든_필드가_유효하면_객체_생성_성공() {
		// arrange
		long userId = 1L;
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(1000));
		LocalDateTime createdAt = LocalDateTime.now();

		// act
		Balance balance = Balance.of(moneyVO, LocalDateTime.now(), userId);

		// assert
		assertThat(balance).isNotNull();
		assertThat(balance.getMoneyVO()).isEqualTo(moneyVO);
		assertThat(balance.getCreatedAt()).isEqualTo(createdAt);
		assertThat(balance.getUserId()).isEqualTo(userId);
	}

}
