package kr.hhplus.be.server.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.balance.BalanceChargeRequest;

class BalanceUnitTest {

	@Test
	void 최대포인트를_초과하면_포인트_충전에_실패한다() {
		// arrange
		long balanceId = 1L;
		long chargePoint = 100000L;
		Balance balance = Balance.builder()
			.id(balanceId)
			.point(BigDecimal.valueOf(1000))
			.build();
		BalanceChargeRequest request = new BalanceChargeRequest(balanceId, chargePoint);

		// act & assert
		assertThatThrownBy(() -> balance.chargePoint(request.toCommand().chargePoint())).isInstanceOf(
				CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_CHARGED_POINT.getMessage());
	}

	@Test
	void 최대포인트를_초과하지_않는다면_포인트_충전에_성공한다() {
		// arrange
		long balanceId = 1L;
		long chargePoint = 9000L;
		Balance balance = Balance.builder()
			.id(balanceId)
			.point(BigDecimal.valueOf(1000))
			.build();
		BalanceChargeRequest request = new BalanceChargeRequest(balanceId, chargePoint);
		//act
		Balance result = balance.chargePoint(request.toCommand().chargePoint());

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

}
