package kr.hhplus.be.server.application.concurrency;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.server.application.reservation.ReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationService;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import kr.hhplus.be.server.infrastructure.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class ReservationConcurrencyTest {

	@Autowired
	private ConcertJpaRepository concertJpaRepository;
	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;
	@Autowired
	private ConcertSeatJpaRepository concertSeatJpaRepository;
	@Autowired
	private UserJpaRepository userJpaRepository;
	@Autowired
	private ReservationService reservationService;
	@Autowired
	private ReservationJpaRepository reservationJpaRepository;

	@Test
	void 동시에_여러_사용자가_예약을_진행하면_한명만_성공한다() throws InterruptedException {
		// arrange
		int userCount = 10;
		Concert concert = concertJpaRepository.save(
			Concert.builder().concertTitle("윤하 콘서트").artistName("윤하").build());

		ConcertSchedule schedule = concertScheduleJpaRepository.save(ConcertSchedule.builder()
			.concertDate(LocalDate.of(2025, 6, 20))
			.venue("성균관대학교")
			.status(
				ConcertScheduleStatus.AVAILABLE)
			.createdAt(LocalDateTime.now())
			.concert(concert)
			.build());

		ConcertSeat seat = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("A")
				.seatNumber(1)
				.status(ConcertSeatStatus.AVAILABLE)
				.build()
		);

		List<Long> userIds = new ArrayList<>();

		for (int i = 0; i < userCount; i++) {
			User user = userJpaRepository.save(User.builder().name("사용자" + i).build());
			userIds.add(user.getId());
		}
		ReservationCommand command = new ReservationCommand(schedule.getId(), schedule.getConcertDate());

		// act
		CountDownLatch latch = new CountDownLatch(userCount);
		ExecutorService executor = Executors.newFixedThreadPool(userCount);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		for (int i = 0; i < userCount; i++) {
			final int count = i;
			executor.submit(() -> {
				try {
					reservationService.reserve(seat.getId(), userIds.get(count), command);
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
		List<Reservation> reservations = reservationJpaRepository.findAll();
		List<ConcertSeat> seats = concertSeatJpaRepository.findAll();
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(9);

		Reservation saved = reservations.get(0);
		assertThat(saved).isNotNull();
		assertThat(saved.getUser()).isNotNull();
		assertThat(saved.getConcertSchedule().getId())
			.isEqualTo(schedule.getId());
		assertThat(saved.getConcertSeat().getId())
			.isEqualTo(seat.getId());

		ConcertSeat concertSeat = seats.get(0);
		assertThat(concertSeat.getStatus()).isEqualTo(ConcertSeatStatus.HELD);

	}

}
