package kr.hhplus.be.server.application.concurrency;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import kr.hhplus.be.server.application.payment.PaymentCommand;
import kr.hhplus.be.server.application.payment.PaymentService;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import kr.hhplus.be.server.infrastructure.payment.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.token.TokenJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class PaymentConcurrencyTest {

	@Autowired
	private UserJpaRepository userJpaRepository;
	@Autowired
	private BalanceJpaRepository balanceJpaRepository;
	@Autowired
	private ConcertJpaRepository concertJpaRepository;
	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;
	@Autowired
	private ConcertSeatJpaRepository concertSeatJpaRepository;
	@Autowired
	private ReservationJpaRepository reservationJpaRepository;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private PaymentJpaRepository paymentJpaRepository;
	@Autowired
	private TokenJpaRepository tokenJpaRepository;

	@Test
	void 사용자의_결제_요청이_동시에_여러번_들어오면_먼저_들어온_요청이외에는_실패한다() throws InterruptedException {
		// arrange
		int payCount = 3;

		User user = userJpaRepository.save(User.builder().name("철수").build());

		balanceJpaRepository.save(
			Balance.create(MoneyVO.create(BigDecimal.valueOf(10000)), LocalDateTime.now(), user.getId()));

		tokenJpaRepository.save(Token.createToken(user));

		Concert concert = concertJpaRepository.save(
			Concert.builder().concertTitle("윤하 콘서트").artistName("윤하").build());

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 2, 22))
				.venue("서울대학교")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build());

		ConcertSeat seat = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("A")
				.seatNumber(1)
				.price(MoneyVO.create(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());
		// 3) 예약(HELD 상태)
		Reservation reservation = reservationJpaRepository.save(
			Reservation.createPendingReservation(
				user, seat, schedule, ReservationStatus.HELD));
		PaymentCommand command = new PaymentCommand(seat.getId(), new BigDecimal("5000"));

		// act
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		// act
		CountDownLatch latch = new CountDownLatch(payCount);
		ExecutorService executor = Executors.newFixedThreadPool(payCount);
		for (int i = 0; i < payCount; i++) {
			executor.submit(() -> {
				try {
					paymentService.paymentSeat(reservation.getId(), user.getId(), command);
					successCount.incrementAndGet();
				} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
					log.error("에러 확인용 {}", e.getMessage());
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

		List<Balance> balanceList = balanceJpaRepository.findAll();
		List<Reservation> reservationList = reservationJpaRepository.findAll();
		List<Payment> paymentList = paymentJpaRepository.findAll();

		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(2);

		assertThat(balanceList.get(0).getMoneyVO().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000L));
		assertThat(paymentList.size()).isEqualTo(1);
		assertThat(reservationList.get(0).getReservationStatus()).isEqualTo(ReservationStatus.BOOKED);
	}
}
