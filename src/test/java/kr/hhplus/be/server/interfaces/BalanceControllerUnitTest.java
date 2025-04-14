package kr.hhplus.be.server.interfaces;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.application.BalanceService;
import kr.hhplus.be.server.domain.MoneyVO;
import kr.hhplus.be.server.domain.balance.model.BalanceInfo;
import kr.hhplus.be.server.interfaces.balance.BalanceChargeRequest;
import kr.hhplus.be.server.interfaces.balance.BalanceController;

@ExtendWith(MockitoExtension.class)
class BalanceControllerUnitTest {

	private MockMvc mockMvc;

	@Mock
	private BalanceService balanceService;

	@InjectMocks
	private BalanceController balanceController;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(balanceController)
			.build();
	}

	@Test
	void 유저_아이디가_유효하다면_포인트_조회_성공() throws Exception {
		// arrange : 테스트에 사용할 데이터 및 모의 행위 설정
		long userId = 1L;
		long balanceId = 1L;
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(1000));

		BalanceInfo result = BalanceInfo.builder()
			.balanceId(userId)
			.moneyVO(moneyVO)
			.userId(balanceId)
			.build();
		//stub
		given(balanceService.getPoint(userId)).willReturn(result);

		//Act & assert
		// GET 요청시 올바른 JSON 응답과 상태코드(200 ok)를 반환하는지 확인
		mockMvc.perform(get("/api/v1/balances/{userId}", userId))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value("포인트 조회 성공"))
			.andExpect(jsonPath("$.info.point").value(BigDecimal.valueOf(1000)))
			.andExpect(jsonPath("$.info.userId").value(userId));

	}

	@Test
	void 유저_아이디와_포인트가_유효하다면_포인트_충전_성공() throws Exception {
		// arrange
		long userId = 1L;
		long balanceId = 1L;
		long chargePointId = 1000L;
		MoneyVO moneyVO = MoneyVO.of(BigDecimal.valueOf(50000));
		// 요청 바디로 사용할 DTO
		BalanceChargeRequest request = new BalanceChargeRequest(balanceId, chargePointId);

		// 서비스가 리턴할 가짜 응답값(스텁)
		BalanceInfo result = BalanceInfo.builder()
			.balanceId(balanceId)              // balance 엔티티 ID
			.moneyVO(moneyVO)  // 잔액(예: 5만 포인트)
			.userId(userId)                    // userId
			.build();

		// Service mock 세팅
		given(balanceService.chargePoint(userId, request.toCommand())).willReturn(result);

		// act & assert
		mockMvc.perform(
				put("/api/v1/balances/{userId}/transactions", userId)  // PUT 요청
					.contentType(MediaType.APPLICATION_JSON)
					.content(new ObjectMapper().writeValueAsString(request)) // JSON 직렬화
			)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value("포인트 충전 성공"))
			.andExpect(jsonPath("$.info.point").value(50000))  // BigDecimal(50000) → JSON에선 숫자 50000
			.andExpect(jsonPath("$.info.userId").value(userId));

		// 추가로 Service 호출이 한번만 일어났는지 검증하고 싶다면:
		verify(balanceService, times(1)).chargePoint(userId, request.toCommand());
		verifyNoMoreInteractions(balanceService);
	}
}

