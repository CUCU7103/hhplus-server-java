package kr.hhplus.be.server.application.integration;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.payment.PaymentCommand;
import kr.hhplus.be.server.application.payment.PaymentInfo;
import kr.hhplus.be.server.application.payment.PaymentService;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import kr.hhplus.be.server.infrastructure.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.token.TokenJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class PaymentIntegrationTest {

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
	private TokenJpaRepository tokenJpaRepository;
	@Autowired
	private PaymentService paymentService;

	@Test
	void 잔액과_좌석_상태가_정상이면_결제에_성공하고_잔액이_차감된다() {
		/* -------- arrange -------- */
		// 1) 유저·잔액
		User user = userJpaRepository.save(User.builder().name("철수").build());
		balanceJpaRepository.save(
			Balance.of(MoneyVO.of(BigDecimal.valueOf(10000)), LocalDateTime.now(), user.getId()));

		// 2) 콘서트/스케줄/좌석
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
				.price(MoneyVO.of(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());
		// 3) 예약(HELD 상태)
		Reservation reservation = reservationJpaRepository.save(
			Reservation.createPendingReservation(
				user, seat, schedule, ReservationStatus.HELD));
		// 4) 토큰
		tokenJpaRepository.save(Token.createToken(user));
		// 5) 결제 명령
		PaymentCommand command = new PaymentCommand(reservation.getId(), BigDecimal.valueOf(5000));
		/* -------- act -------- */
		PaymentInfo info = paymentService.paymentSeat(
			reservation.getId(), user.getId(), command);

		/* -------- assert -------- */
		// 반환 객체
		assertThat(info.userId()).isEqualTo(user.getId());
		assertThat(info.amount()).isEqualByComparingTo(command.amount());
	}

	@Test
	void 잔액이_부족하면_NOT_ENOUGH_BALANCE_예외가_발생하고_롤백된다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("영희").build());
		balanceJpaRepository.save(
			Balance.of(MoneyVO.of(new BigDecimal("100")), LocalDateTime.now(), user.getId())); // 100원뿐

		Concert concert = concertJpaRepository.save(
			Concert.builder().concertTitle("테스트").artistName("테스트").build());
		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 5, 5))
				.venue("테스트홀")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build());
		ConcertSeat seat = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("B")
				.seatNumber(1)
				.status(ConcertSeatStatus.AVAILABLE)
				.build());
		Reservation reservation = reservationJpaRepository.save(
			Reservation.createPendingReservation(
				user, seat, schedule, ReservationStatus.HELD));
		tokenJpaRepository.save(Token.createToken(user));

		PaymentCommand command = new PaymentCommand(seat.getId(), new BigDecimal("5000")); // 부족

		// act & assert
		assertThatThrownBy(() ->
			paymentService.paymentSeat(reservation.getId(), user.getId(), command))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.OVER_USED_POINT.getMessage());

		Balance bal = balanceJpaRepository.findById(user.getId()).orElseThrow();
		assertThat(bal.getMoneyVO().getAmount()).isEqualByComparingTo("100");
		assertThat(concertSeatJpaRepository.findById(seat.getId()).orElseThrow().getStatus())
			.isEqualTo(ConcertSeatStatus.AVAILABLE);
		assertThat(reservationJpaRepository.findById(reservation.getId()).orElseThrow().getReservationStatus())
			.isEqualTo(ReservationStatus.HELD);

	}

	@Test
	void 한_사용자가_동시에_여러_스레드에서_결제를_시도하면_잔액_불일치_문제가_발생한다() throws InterruptedException {
		/* -------- arrange -------- */
		// 1) 사용자 한 명 생성 및 정확히 한 번의 결제만 가능한 잔액 설정
		User user = userJpaRepository.save(User.builder().name("철수").build());
		balanceJpaRepository.save(
			Balance.of(MoneyVO.of(BigDecimal.valueOf(5000)), LocalDateTime.now(), user.getId()));
		tokenJpaRepository.save(Token.createToken(user));

		// 2) 콘서트/스케줄 설정
		Concert concert = concertJpaRepository.save(
			Concert.builder().concertTitle("테스트 콘서트").artistName("테스트").build());

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 4, 20))
				.venue("테스트홀")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build());

		// 3) 두 개의 서로 다른 좌석 생성
		ConcertSeat seat1 = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("A")
				.seatNumber(1)
				.price(MoneyVO.of(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());

		ConcertSeat seat2 = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("A")
				.seatNumber(2)
				.price(MoneyVO.of(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());

		// 4) 두 좌석 각각에 대한 예약 생성
		Reservation reservation1 = reservationJpaRepository.save(
			Reservation.createPendingReservation(
				user, seat1, schedule, ReservationStatus.HELD));

		Reservation reservation2 = reservationJpaRepository.save(
			Reservation.createPendingReservation(
				user, seat2, schedule, ReservationStatus.HELD));

		// 5) 동시 실행을 위한 설정
		int numberOfThreads = 2;
		CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

		AtomicInteger successCount = new AtomicInteger(0);
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

		/* -------- act -------- */
		// 두 개의 스레드에서 동시에 결제 시도 (사용자는 하나, 잔액은 한 번만 결제할 수 있는 금액)
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

		// 첫 번째 좌석 결제 시도
		executorService.submit(() -> {
			try {
				PaymentCommand command = new PaymentCommand(seat1.getId(), BigDecimal.valueOf(5000));
				readyLatch.countDown();
				startLatch.await();

				paymentService.paymentSeat(reservation1.getId(), user.getId(), command);
				successCount.incrementAndGet();
			} catch (Exception e) {
				exceptions.add(e);
			} finally {
				finishLatch.countDown();
			}
		});

		// 두 번째 좌석 결제 시도
		executorService.submit(() -> {
			try {
				PaymentCommand command = new PaymentCommand(seat2.getId(), BigDecimal.valueOf(5000));
				readyLatch.countDown();
				startLatch.await();

				paymentService.paymentSeat(reservation2.getId(), user.getId(), command);
				successCount.incrementAndGet();
			} catch (Exception e) {
				exceptions.add(e);
			} finally {
				finishLatch.countDown();
			}
		});

		// 모든 스레드가 준비될 때까지 대기
		readyLatch.await(5, TimeUnit.SECONDS);

		// 시작 신호 발생
		startLatch.countDown();

		// 모든 스레드가 완료될 때까지 대기
		finishLatch.await(10, TimeUnit.SECONDS);
		executorService.shutdown();

		/* -------- assert -------- */
		// 락이 없으면 두 결제 모두 성공할 가능성이 있음 (잔액은 한 건만 가능한데!)
		// 이것이 잔액에 대한 동시성 문제가 있음을 보여주는 것
		assertThat(successCount.get()).isEqualTo(2);

		// 사용자의 잔액이 마이너스가 되었는지 확인 (이것이 문제점)
		Balance updatedBalance = balanceJpaRepository.findById(user.getId()).orElseThrow();
		assertThat(updatedBalance.getMoneyVO().getAmount()).isLessThan(BigDecimal.ZERO);

		// 두 예약 모두 BOOKED 상태인지 확인
		Reservation updatedReservation1 = reservationJpaRepository.findById(reservation1.getId()).orElseThrow();
		Reservation updatedReservation2 = reservationJpaRepository.findById(reservation2.getId()).orElseThrow();

		assertThat(updatedReservation1.getReservationStatus()).isEqualTo(ReservationStatus.BOOKED);
		assertThat(updatedReservation2.getReservationStatus()).isEqualTo(ReservationStatus.BOOKED);

		// 두 좌석 모두 BOOKED 상태인지 확인
		assertThat(concertSeatJpaRepository.findById(seat1.getId()).orElseThrow().getStatus())
			.isEqualTo(ConcertSeatStatus.BOOKED);
		assertThat(concertSeatJpaRepository.findById(seat2.getId()).orElseThrow().getStatus())
			.isEqualTo(ConcertSeatStatus.BOOKED);

		// 이 테스트가 통과한다면 잔액에 대한 동시성 문제가 있다는 것을 확인하는 것
	}
}
