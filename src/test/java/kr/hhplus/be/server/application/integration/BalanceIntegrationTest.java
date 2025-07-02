package kr.hhplus.be.server.application.integration;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.balance.BalanceInfo;
import kr.hhplus.be.server.application.balance.BalanceService;
import kr.hhplus.be.server.application.balance.ChargeBalanceCommand;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@Transactional
@SpringBootTest
@Slf4j
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
			Balance.create(MoneyVO.create(BigDecimal.valueOf(1000)), LocalDateTime.now(), user.getId()));

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
			Balance.create(MoneyVO.create(BigDecimal.valueOf(1000)), LocalDateTime.now(), user.getId()));
		// act
		ChargeBalanceCommand command = new ChargeBalanceCommand(balance.getId(), BigDecimal.valueOf(1000));
		BalanceInfo balanceInfo = balanceService.chargePoint(user.getId(), command);
		// assert
		assertThat(balanceInfo.userId()).isEqualTo(user.getId());
		assertThat(balanceInfo.balanceId()).isEqualTo(balance.getId());
		assertThat(balanceInfo.moneyVO()
			.getAmount()
			.compareTo(BigDecimal.valueOf(2000))).isZero();
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	/**
	 * 테스트 데이터가 커밋되지 않음
	 * 대다수의 Spring Boot 테스트는 클래스나 메서드에 @Transactional이 붙어 있고, 테스트 종료 시 롤백됩니다.
	 * userJpaRepository.saveAndFlush(...) 는 같은 트랜잭션 내에서 DB에 반영(flush)만 할 뿐, 커밋(commit)은 하지 않습니다.
	 * 별도의 스레드에서 실행되는 chargePoint()는 새로운 트랜잭션(Propagation.REQUIRED)으로 사용자 조회를 시도하는데, 이 시점에 테스트 트랜잭션이 아직 커밋되지 않았기 때문에 DB에 존재하지 않는 것으로 보입니다.
	 *
	 * 트랜잭션 격리(isolation)
	 * MySQL 기본 격리 수준(REPEATABLE_READ) 하에서는 다른 커넥션이 아직 커밋하지 않은 데이터를 절대 볼 수 없습니다.
	 * 따라서, 테스트 메서드의 트랜잭션 밖에서 실행되는 스레드들은 “아직 존재하지 않는” 사용자 정보를 조회하다가 NOT_FOUND_USER 예외가 발생합니다.
	 */
	void 사용자가_여러번_충전을_시도했을때_한_번만_충전이_진행된다() throws InterruptedException {
		// arrange
		User user = userJpaRepository.saveAndFlush(User.builder().name("철수").build());
		log.info(String.valueOf(user.getId()));
		log.info(user.toString());
		Balance balance = balanceJpaRepository.saveAndFlush(
			Balance.create(MoneyVO.create(BigDecimal.valueOf(1000)), LocalDateTime.now(), user.getId()));

		// act
		ExecutorService executor = Executors.newFixedThreadPool(3);
		CountDownLatch latch = new CountDownLatch(3);

		ChargeBalanceCommand command = new ChargeBalanceCommand(balance.getId(), BigDecimal.valueOf(1000));

		for (int i = 0; i < 3; i++) {
			executor.submit(() -> {
				try {
					balanceService.chargePoint(user.getId(), command);
					log.info("{} 충전 시도 완료", Thread.currentThread().getName());
				} catch (Exception e) {
					log.info(e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		// cleanup
		executor.shutdown(); // 스레드 풀 종료

		// assert
		Balance foundBalance = balanceJpaRepository.findById(balance.getId())
			.orElseThrow(() -> new AssertionError("잔액 정보를 찾을 수 없습니다.")); // ID로 다시 조회

		// 예상 결과: 1000
		BigDecimal expectedAmount = BigDecimal.valueOf(2000);
		// JUnit이나 AssertJ 같은 라이브러리를 사용해서 값을 비교합니다.
		assertThat(foundBalance.getMoneyVO().getAmount()).isEqualByComparingTo(expectedAmount);

	}

}
