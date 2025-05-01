package kr.hhplus.be.server.application.concurrency;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.application.balance.ChargeBalanceCommand;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceRepository;
import kr.hhplus.be.server.domain.balance.history.BalanceHistory;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.balance.BalanceHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class BalanceConcurrencyTest {

	@Autowired
	private UserJpaRepository userJpaRepository;
	@Autowired
	private BalanceJpaRepository balanceJpaRepository;
	@Autowired
	private BalanceService balanceService;
	@Autowired
	private BalanceRepository balanceRepository;
	@Autowired
	private BalanceHistoryJpaRepository balanceHistoryJpaRepository;

	@Test
	void 사용자가_중복으로_포인트_충전을_진행했을때_먼저_들어온_요청만_성공시킨다() throws InterruptedException {
		// arrange
		int chargeCount = 3;
		User user = userJpaRepository.saveAndFlush(User.builder().name("사용자").build());
		Balance balance = balanceJpaRepository.saveAndFlush(
			Balance.create(MoneyVO.create(BigDecimal.valueOf(1000)), LocalDateTime.now(), user.getId()));
		ChargeBalanceCommand command1 = new ChargeBalanceCommand(balance.getId(), BigDecimal.valueOf(2000));

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		// act
		CountDownLatch latch = new CountDownLatch(chargeCount);
		ExecutorService executor = Executors.newFixedThreadPool(chargeCount);
		for (int i = 0; i < chargeCount; i++) {
			executor.submit(() -> {
				try {
					balanceService.chargePoint(user.getId(), command1);
					successCount.incrementAndGet();
				} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
					failCount.incrementAndGet(); // 도메인 예외 포함 (락 충돌 등)
				} catch (Exception e) {
					failCount.incrementAndGet(); // 예외 catch 누락 방지
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executor.shutdown();
		// assert
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(2);

		// ② 히스토리 저장 검증
		List<BalanceHistory> histories = balanceHistoryJpaRepository.findAll();
		assertThat(histories).hasSize(1);

		BalanceHistory saved = histories.get(0);
		// ③ 이력 필드 검증: 충전 전(previousPoint)과 충전(deltaPoint) 값이 제대로 들어갔는지 확인
		assertThat(saved.getBalance().getId()).isEqualTo(balance.getId());
		assertThat(saved.getDeltaPoint().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
		assertThat(saved.getPreviousPoint().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
	}

}
