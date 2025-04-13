package kr.hhplus.be.server.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.model.PointVO;
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
			.point(BigDecimal.valueOf(1000))
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
			.point(BigDecimal.valueOf(1000))
			.build();
		//act
		Balance result = balance.chargePoint(chargePoint);

		// assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(balanceId);
		assertThat(result.getPoint()).isEqualTo(BigDecimal.valueOf(10000));
	}

	@Test
	void 새로운_밸런스를_객체를_생성하며_유저와_연결한다() {
		// given
		User user = User.builder()
			.id(123L)
			.name("홍길동")
			.build();

		//act
		Balance createBalance = Balance.of(BigDecimal.valueOf(1000), LocalDateTime.now(), user);

		// assert
		assertThat(user).isEqualTo(createBalance.getUser());
		assertThat(BigDecimal.valueOf(1000)).isEqualTo(createBalance.getPoint());
		assertThat(createBalance.getCreatedAt()).isNotNull();
	}

	@Test
	void 보유한_포인트_이상_사용시_예외처리() {
		// arrange
		long balanceId = 1L;
		BigDecimal usePoint = BigDecimal.valueOf(100000);

		Balance balance = Balance.builder()
			.id(balanceId)
			.point(BigDecimal.valueOf(1000))
			.build();

		// assert
		assertThatThrownBy(() -> balance.usePoint(usePoint)).isInstanceOf(
				CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_USED_POINT.getMessage());
	}

	@Test
	void 결제금액이_보유한_포인트_이하라면_사용가능() {
		// arrange
		long balanceId = 1L;
		BigDecimal usePoint = BigDecimal.valueOf(5000);

		Balance balance = Balance.builder()
			.id(balanceId)
			.point(BigDecimal.valueOf(20000))
			.build();

		Balance result = balance.usePoint(usePoint);
		// assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(balanceId);
		assertThat(result.getPoint()).isEqualTo(BigDecimal.valueOf(15000));
	}

}
