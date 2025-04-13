package kr.hhplus.be.server.domain.balance;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.balance.model.PointVO;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;

public class PointVOUnitTest {

	@Test
	void 유효한_포인트로_객체_생성_성공() {
		// arrange
		BigDecimal amount = BigDecimal.valueOf(10000);

		// act
		PointVO pointVO = PointVO.of(amount);

		// assert
		assertThat(pointVO).isNotNull();
		assertThat(pointVO.getAmount()).isEqualTo(amount);
	}

	@Test
	void 음수_포인트_생성시_예외발생() {
		// arrange
		BigDecimal negativeAmount = BigDecimal.valueOf(-100);

		// act & assert
		assertThatThrownBy(() -> PointVO.of(negativeAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_USED_POINT.getMessage());
	}

	@Test
	void 최대치_초과_포인트_생성시_예외발생() {
		// arrange
		BigDecimal overMaxAmount = BigDecimal.valueOf(100_001);

		// act & assert
		assertThatThrownBy(() -> PointVO.of(overMaxAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_CHARGED_POINT.getMessage());
	}

	@Test
	void 최대치_포인트로_객체_생성_성공() {
		// arrange
		BigDecimal maxAmount = BigDecimal.valueOf(100_000);

		// act
		PointVO pointVO = PointVO.of(maxAmount);

		// assert
		assertThat(pointVO).isNotNull();
		assertThat(pointVO.getAmount()).isEqualTo(maxAmount);
	}

	@Test
	void 포인트_합산_테스트() {
		// arrange
		PointVO pointVO = PointVO.of(BigDecimal.valueOf(5000));
		BigDecimal addAmount = BigDecimal.valueOf(3000);

		// act
		PointVO result = pointVO.add(addAmount);

		// assert
		assertThat(result).isNotNull();
		assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(8000));
	}

	@Test
	void 포인트_차감_테스트() {
		// arrange
		PointVO pointVO = PointVO.of(BigDecimal.valueOf(5000));
		BigDecimal subtractAmount = BigDecimal.valueOf(3000);

		// act
		PointVO result = pointVO.subtract(subtractAmount);

		// assert
		assertThat(result).isNotNull();
		assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(2000));
	}

	@Test
	void 포인트_차감시_음수되면_예외발생() {
		// arrange
		PointVO pointVO = PointVO.of(BigDecimal.valueOf(1000));
		BigDecimal subtractAmount = BigDecimal.valueOf(2000);

		// act & assert
		assertThatThrownBy(() -> pointVO.subtract(subtractAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_USED_POINT.getMessage());
	}

	@Test
	void 포인트_합산시_최대치_초과하면_예외발생() {
		// arrange
		PointVO pointVO = PointVO.of(BigDecimal.valueOf(90_000));
		BigDecimal addAmount = BigDecimal.valueOf(20_000);

		// act & assert
		assertThatThrownBy(() -> pointVO.add(addAmount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_CHARGED_POINT.getMessage());
	}

}
