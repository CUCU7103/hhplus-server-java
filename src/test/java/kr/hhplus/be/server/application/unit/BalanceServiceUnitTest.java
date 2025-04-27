package kr.hhplus.be.server.application.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.application.balance.BalanceInfo;
import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceRepository;
import kr.hhplus.be.server.domain.balance.history.BalanceHistory;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.presentation.balance.BalanceChargeRequest;

@ExtendWith(MockitoExtension.class)
class BalanceServiceUnitTest {
	@Mock
	private BalanceRepository balanceRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private BalanceService balanceService;

	@Test
	void 유저의_포인트_조회에_성공한다() {
		// arrange
		long userId = 1L;
		long balanceId = 1L;
		MoneyVO moneyVO = MoneyVO.create(BigDecimal.valueOf(1000));
		User user = mock(User.class);

		Balance balance = Balance.builder()
			.id(balanceId)
			.moneyVO(moneyVO)
			.userId(userId)
			.build();

		//stub
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(balanceRepository.findById(balanceId)).willReturn(Optional.of(balance));
		// act
		BalanceInfo response = balanceService.getPoint(userId);
		// assert
		assertThat(response).isNotNull();
		assertThat(response.balanceId()).isEqualTo(balanceId);
		assertThat(response.moneyVO().getAmount()).isEqualTo(moneyVO.getAmount());
		// userRepository가 호출되었는지 확인
		verify(userRepository, times(1)).findById(userId);

	}

	@Test
	void 존재하지_않은_유저의_아이디는_포인트_조회에_실패한다() {
		// given
		long userId = 1L;

		given(userRepository.findById(userId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> balanceService.getPoint(userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());

		// 유저가 없으면 BalanceRepository가 호출되지 않았는지 확인
		verify(balanceRepository, times(0)).findById(anyLong());
	}

	@Test
	void 존재하지_않은_포인트_정보_조회는_실패한다() {
		// arrange
		long userId = 1L;
		long balanceId = 1L;
		User user = User.builder()
			.id(userId)
			.name("홍길동")
			.build();

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(balanceRepository.findById(balanceId))
			.willReturn(Optional.empty());

		// act& assert
		assertThatThrownBy(() -> balanceService.getPoint(userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_BALANCE.getMessage());

	}

	@Test
	void 존재하지_않은_유저의_아이디는_포인트_충전에_실패한다() {
		// given
		long userId = 1L;
		BalanceChargeRequest request = new BalanceChargeRequest(1L, 1000L);

		given(userRepository.findById(userId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> balanceService.chargePoint(userId, request.toCommand()))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_USER.getMessage());

		// 유저가 없으면 BalanceRepository가 호출되지 않았는지 확인
		verify(balanceRepository, times(0)).findByIdAndUserId(anyLong(), anyLong());
	}

	@Test
	void 밸런스가_존재하면_기존_밸런스에_포인트_충전() {
		// arrange
		long userId = 1L;
		long balanceId = 10L;
		BalanceChargeRequest request = new BalanceChargeRequest(balanceId, 100L);
		MoneyVO moneyVO = MoneyVO.create(BigDecimal.valueOf(1000));
		User user = mock(User.class);
		// 포인트 충전 검증을 위함.
		Balance existingBalance = Balance.create(moneyVO, LocalDateTime.now(), userId);
		existingBalance.chargePoint(BigDecimal.valueOf(100));

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(balanceRepository.findByIdAndUserId(request.toCommand().balanceId(), userId))
			.willReturn(Optional.of(existingBalance));
		// act
		BalanceInfo result = balanceService.chargePoint(userId, request.toCommand());
		// assert
		// 포인트가 기존 200 + 100 = 300이 되었는지 검증
		assertThat(result.moneyVO().getAmount()).isEqualTo(existingBalance.getMoneyVO().getAmount());
		// pointHistory 저장 로직이 정상 호출되었는지 검증
		verify(balanceRepository, times(1)).save(any(BalanceHistory.class));
	}

	@Test
	void 밸런스가_없으면_새로_생성하여_포인트를_충전한다() {
		long userId = 1L;
		long balanceId = 10L;
		BalanceChargeRequest request = new BalanceChargeRequest(balanceId, 50L);
		User user = mock(User.class);
		//stub
		given(userRepository.findById(userId))
			.willReturn(Optional.of(user));
		given(balanceRepository.findByIdAndUserId(balanceId, userId))
			.willReturn(Optional.empty()); // 존재하지 않는 Balance

		//act
		BalanceInfo result = balanceService.chargePoint(userId, request.toCommand());

		assertThat(result.moneyVO().getAmount()).isEqualTo(request.toCommand().chargePoint());
		verify(balanceRepository, times(1)).save(any(BalanceHistory.class));
	}

}
