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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import kr.hhplus.be.server.infrastructure.payment.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.reservation.ReservationJpaRepository;
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

	@BeforeEach
	void clear() {
		// 외래 키 제약 조건을 고려하여 삭제 순서 조정
		paymentJpaRepository.deleteAll();
		reservationJpaRepository.deleteAll();
		concertSeatJpaRepository.deleteAll();
		concertScheduleJpaRepository.deleteAll();
		concertJpaRepository.deleteAll();
		balanceJpaRepository.deleteAll();
		userJpaRepository.deleteAll();

	}

	@Test
	void 사용자의_결제_요청이_동시에_여러번_들어오면_먼저_들어온_요청이외에는_실패한다() throws InterruptedException {
		// arrange
		int payCount = 3;

		User user = userJpaRepository.saveAndFlush(User.builder().name("철수").build());

		balanceJpaRepository.saveAndFlush(
			Balance.create(MoneyVO.create(BigDecimal.valueOf(10000)), LocalDateTime.now(), user.getId()));

	/*	Token token = tokenJpaRepository.save(Token.createToken(user));
		ReflectionTestUtils.setField(token, "status", TokenStatus.ACTIVE);
		tokenJpaRepository.saveAndFlush(token);*/

		Concert concert = concertJpaRepository.saveAndFlush(
			Concert.builder().concertTitle("윤하 콘서트").artistName("윤하").build());

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 2, 22))
				.venue("서울대학교")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build());

		ConcertSeat seat = concertSeatJpaRepository.saveAndFlush(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("A")
				.seatNumber(1)
				.price(MoneyVO.create(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());
		// 3) 예약(HELD 상태)
		Reservation reservation = reservationJpaRepository.saveAndFlush(
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
					paymentService.payment(reservation.getId(), user.getId(), command);
					successCount.incrementAndGet();
				} catch (CustomException e) {
					log.error("에러 확인용 {}", e.getMessage());
					failCount.incrementAndGet(); // 도메인 예외 포함 (락 충돌 등)
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

	@Test
	void 사용자가_가진_토큰이_유효한_동안_시도하는_각기_다른_예약에_대한_결제는_성공한다() throws InterruptedException {
		// arrange
		User user = userJpaRepository.saveAndFlush(User.builder().name("영희").build());

		balanceJpaRepository.saveAndFlush(
			Balance.create(MoneyVO.create(BigDecimal.valueOf(15000)), LocalDateTime.now(), user.getId()));

	/*	Token token = tokenJpaRepository.save(Token.createToken(user));
		ReflectionTestUtils.setField(token, "status", TokenStatus.ACTIVE);
		tokenJpaRepository.saveAndFlush(token);*/

		Concert concert = concertJpaRepository.save(
			Concert.builder().concertTitle("아이유 콘서트").artistName("아이유").build());

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 5, 10))
				.venue("코엑스 아티움")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build());

		ConcertSeat seat1 = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("B")
				.seatNumber(1)
				.price(MoneyVO.create(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());

		ConcertSeat seat2 = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("B")
				.seatNumber(2)
				.price(MoneyVO.create(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());

		Reservation reservation1 = reservationJpaRepository.save(
			Reservation.createPendingReservation(user, seat1, schedule, ReservationStatus.HELD));

		Reservation reservation2 = reservationJpaRepository.save(
			Reservation.createPendingReservation(user, seat2, schedule, ReservationStatus.HELD));

		PaymentCommand command1 = new PaymentCommand(seat1.getId(), new BigDecimal("5000"));
		PaymentCommand command2 = new PaymentCommand(seat2.getId(), new BigDecimal("5000"));

		// act
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		CountDownLatch latch = new CountDownLatch(2);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		executor.submit(() -> {
			try {
				paymentService.payment(reservation1.getId(), user.getId(), command1);
				successCount.incrementAndGet();
			} catch (Exception e) {
				failCount.incrementAndGet();
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});

		executor.submit(() -> {
			try {
				paymentService.payment(reservation2.getId(), user.getId(), command2);
				successCount.incrementAndGet();
			} catch (Exception e) {
				failCount.incrementAndGet();
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});

		latch.await();
		executor.shutdown();

		List<Balance> balanceList = balanceJpaRepository.findAll();
		List<Payment> paymentList = paymentJpaRepository.findAll();

		assertThat(successCount.get()).isEqualTo(2);
		assertThat(balanceList.get(0).getMoneyVO().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000L));
		assertThat(paymentList.size()).isEqualTo(2);
	}

}
