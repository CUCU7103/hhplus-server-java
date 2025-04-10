package kr.hhplus.be.server.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.BalanceHistory;
import kr.hhplus.be.server.domain.balance.model.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.balance.model.BalanceInfo;
import kr.hhplus.be.server.domain.balance.model.BalanceRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.balance.BalanceChargeRequest;

@ExtendWith(MockitoExtension.class)
class BalanceServiceUnitTest {
	@Mock
	private BalanceRepository balanceRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BalanceHistoryRepository balanceHistoryRepository;

	@InjectMocks
	private BalanceService balanceService;

	@Test
	void 유저의_포인트_조회에_성공한다() {
		// arrange
		long userId = 1L;
		long balanceId = 1L;
		User user = User.builder()
			.id(userId)
			.name("홍길동")
			.build();

		Balance balance = Balance.builder()
			.id(balanceId)
			.point(BigDecimal.valueOf(1000))
			.user(user)
			.build();

		//stub
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(balanceRepository.findById(balanceId)).willReturn(Optional.of(balance));
		// act
		BalanceInfo response = balanceService.getPoint(userId);
		// assert
		assertThat(response).isNotNull();
		assertThat(response.userId()).isEqualTo(userId);
		assertThat(response.balanceId()).isEqualTo(balanceId);
		assertThat(response.point()).isEqualTo(BigDecimal.valueOf(1000));

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
		assertThatThrownBy(() -> balanceService.chargePoint(userId, request))
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
		User user = User.builder()
			.id(userId)
			.name("홍길동")
			.build();

		Balance existingBalance = Balance.builder()
			.id(balanceId)
			.point(BigDecimal.valueOf(100))
			.user(user)
			.build();

		existingBalance.chargePoint(BigDecimal.valueOf(200L)); // 초기값(200)이라고 가정

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(balanceRepository.findByIdAndUserId(request.toCommand().balanceId(), userId))
			.willReturn(Optional.of(existingBalance));

		// act
		BalanceInfo result = balanceService.chargePoint(userId, request);

		// assert
		// 포인트가 기존 200 + 100 = 300이 되었는지 검증
		assertThat(BigDecimal.valueOf(400)).isEqualTo(result.point());
		// pointHistory 저장 로직이 정상 호출되었는지 검증
		verify(balanceHistoryRepository, times(1)).save(any(BalanceHistory.class));
	}

	@Test
	void 밸런스가_없으면_새로_생성하여_포인트를_충전한다() {
		long userId = 1L;
		long balanceId = 10L;
		BalanceChargeRequest request = new BalanceChargeRequest(balanceId, 50L);

		User user = User.builder()
			.id(userId)
			.name("홍길동")
			.build();

		//stub
		given(userRepository.findById(userId))
			.willReturn(Optional.of(user));
		given(balanceRepository.findByIdAndUserId(balanceId, userId))
			.willReturn(Optional.empty()); // 존재하지 않는 Balance

		//act
		BalanceInfo result = balanceService.chargePoint(userId, request);

		assertThat(BigDecimal.valueOf(50)).isEqualTo(result.point());
		verify(balanceHistoryRepository, times(1)).save(any(BalanceHistory.class));
	}

}
