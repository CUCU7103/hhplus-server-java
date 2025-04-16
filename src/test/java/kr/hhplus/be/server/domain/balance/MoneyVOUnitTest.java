package kr.hhplus.be.server.domain.balance;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public class MoneyVOUnitTest {

	@Test
	void 유효한_포인트로_객체_생성_성공() {
		// arrange
		BigDecimal amount = BigDecimal.valueOf(10000);

		// act
		MoneyVO moneyVO = MoneyVO.of(amount);

		// assert
		assertThat(moneyVO).isNotNull();
		assertThat(moneyVO.getAmount()).isEqualTo(amount);
	}

	@Test
	void 음수_포인트_생성시_예외발생() {
		// arrange
		BigDecimal negativeAmount = BigDecimal.valueOf(-100);

		// act & assert
		assertThatThrownBy(() -> MoneyVO.of(negativeAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_USED_POINT.getMessage());
	}

	@Test
	void 최대치_초과_포인트_생성시_예외발생() {
		// arrange
		BigDecimal overMaxAmount = BigDecimal.valueOf(100_001);

		// act & assert
		assertThatThrownBy(() -> MoneyVO.of(overMaxAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_CHARGED_POINT.getMessage());
	}

	@Test
	void 최대치_포인트로_객체_생성_성공() {
		// arrange
		BigDecimal maxAmount = BigDecimal.valueOf(100_000);

		// act
		MoneyVO moneyVO = MoneyVO.of(maxAmount);

		// assert
		assertThat(moneyVO).isNotNull();
		assertThat(moneyVO.getAmount()).isEqualTo(maxAmount);
	}

	@Test
	void 포인트_합산_테스트() {
		// arrange
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(5000));
		BigDecimal addAmount = BigDecimal.valueOf(3000);

		// act
		MoneyVO result = moneyVO.add(addAmount);

		// assert
		assertThat(result).isNotNull();
		assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(8000));
	}

	@Test
	void 포인트_차감_테스트() {
		// arrange
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(5000));
		BigDecimal subtractAmount = BigDecimal.valueOf(3000);

		// act
		MoneyVO result = moneyVO.subtract(subtractAmount);

		// assert
		assertThat(result).isNotNull();
		assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(2000));
	}

	@Test
	void 포인트_차감시_음수되면_예외발생() {
		// arrange
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(1000));
		BigDecimal subtractAmount = BigDecimal.valueOf(2000);

		// act & assert
		assertThatThrownBy(() -> moneyVO.subtract(subtractAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_USED_POINT.getMessage());
	}

	@Test
	void 포인트_합산시_최대치_초과하면_예외발생() {
		// arrange
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(90_000));
		BigDecimal addAmount = BigDecimal.valueOf(20_000);

		// act & assert
		assertThatThrownBy(() -> moneyVO.add(addAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_CHARGED_POINT.getMessage());
	}

}
