package kr.hhplus.be.server.application.integration;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.reservation.ReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationInfo;
import kr.hhplus.be.server.application.reservation.ReservationService;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import kr.hhplus.be.server.infrastructure.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@Transactional // 각 테스트가 독립적으로 실행되어진다.
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class ReservationIntegrationTest {

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private BalanceJpaRepository balanceJpaRepository;

	@Autowired
	private ConcertSeatJpaRepository concertSeatJpaRepository;

	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;

	@Autowired
	private ConcertJpaRepository concertJpaRepository;

	@Autowired
	private ReservationJpaRepository reservationJpaRepository;
	@Autowired
	private ReservationService reservationService;

	@Test
	void 유효한_스케줄_좌석_사용자_정보라면_예약에_성공한다() {
		// arrange
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
		// act
		ReservationInfo reservationInfo = ReservationInfo.from(reservationJpaRepository.save(
			Reservation.createPendingReservation(user, seat, schedule,
				ReservationStatus.HELD)));
		// assert
		assertThat(reservationInfo).isNotNull();
		assertThat(reservationInfo.status()).isEqualTo(ReservationStatus.HELD);
		assertThat(reservationInfo.userId()).isEqualTo(user.getId());
		assertThat(reservationInfo.price().getAmount()).isEqualByComparingTo(seat.getPrice().getAmount());

	}

	@Test
	void 만료시간이_지난_예약_취소에_성공한다() {
		// arrange
		// 예약 더미 데이터 생성
		User user = userJpaRepository.save(User.builder().name("철수").build());
		balanceJpaRepository.save(
			Balance.of(MoneyVO.of(BigDecimal.valueOf(10000)), LocalDateTime.now(), user.getId()));

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

		for (int i = 0; i < 3; i++) {
			concertSeatJpaRepository.save(
				ConcertSeat.builder()
					.concertSchedule(schedule)
					.section("A")
					.seatNumber(i)
					.price(MoneyVO.of(BigDecimal.valueOf(5000)))
					.status(ConcertSeatStatus.AVAILABLE)
					.build());
		}
		List<ConcertSeat> seats = concertSeatJpaRepository.findAll();

		for (ConcertSeat seat : seats) {
			reservationJpaRepository.save(Reservation.builder()
				.price(MoneyVO.of(BigDecimal.valueOf(4000)))
				.concertSchedule(schedule)
				.concertSeat(seat)
				.user(user)
				.reservationStatus(ReservationStatus.HELD).build());
		}
		List<Reservation> reservations = reservationJpaRepository.findAll();

		for (Reservation reservation : reservations) {
			ReflectionTestUtils.setField(reservation, "expirationAt", LocalDateTime.now().minusMinutes(10));
		}

		reservationJpaRepository.saveAll(reservations);
		// act
		reservationService.concertReservationCancel();
		// assert
		assertThat(reservationJpaRepository.findByReservationStatus(ReservationStatus.AVAILABLE)).hasSize(3);

	}

	@Test
	void 동시에_여러명이_같은_좌석을_예약하면_중복_예약이_발생할_수_있다() throws InterruptedException {
		// arrange
		User user1 = userJpaRepository.save(User.builder().name("철수").build());
		User user2 = userJpaRepository.save(User.builder().name("영희").build());
		List<User> users = Arrays.asList(user1, user2);

		for (User user : users) {
			balanceJpaRepository.save(
				Balance.of(MoneyVO.of(BigDecimal.valueOf(20000)), LocalDateTime.now(), user.getId()));
		}

		Concert concert = concertJpaRepository.save(
			Concert.builder().concertTitle("테스트 콘서트").artistName("가수").build());

		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 5, 22))
				.venue("서울대학교")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build());

		log.info("스케줄 아이디 {} ", schedule.getId());

		ConcertSeat seat = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("VIP")
				.seatNumber(1)
				.price(MoneyVO.of(BigDecimal.valueOf(10000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());

		ReservationCommand command = new ReservationCommand(schedule.getId(), schedule.getConcertDate());

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch latch = new CountDownLatch(2);

		List<Future<ReservationInfo>> results = new ArrayList<>();

		// act
		results.add(executor.submit(() -> {
			try {
				return reservationService.reservationSeat(seat.getId(), user1.getId(), command);
			} catch (Exception e) {
				System.out.println("User1 실패: " + e.getMessage());
				return null;
			} finally {
				latch.countDown();
			}
		}));

		results.add(executor.submit(() -> {
			try {
				return reservationService.reservationSeat(seat.getId(), user2.getId(), command);
			} catch (Exception e) {
				System.out.println("User2 실패: " + e.getMessage());
				return null;
			} finally {
				latch.countDown();
			}
		}));

		latch.await();

		// assert
		List<Reservation> all = reservationJpaRepository.findAll();
		System.out.println("전체 예약 건수 = " + all.size());

		// ❌ 실패 조건: 예약이 두 건 이상이면 동시성 문제가 발생한 것
		assertThat(all.size()).isGreaterThan(1); // 실제 서비스에선 이건 금지되어야 함
	}

}
