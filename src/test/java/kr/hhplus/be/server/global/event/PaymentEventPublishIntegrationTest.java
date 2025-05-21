package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.payment.PaymentCommand;
import kr.hhplus.be.server.application.payment.PaymentInfo;
import kr.hhplus.be.server.application.payment.PaymentService;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.balance.BalanceJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;
import kr.hhplus.be.server.infrastructure.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
public class PaymentEventPublishIntegrationTest {

	@Autowired
	private ApplicationEvents applicationEvents; // 기록된 이벤트에 접근하기 위한 API

	@Autowired
	private PaymentService paymentService;
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
	private ConcertRankRepository concertRankRepository;

	// 이벤트가 정상적으로 발행되어지는지를 @RecordApplicationEvents를 사용해서 테스트한다.
	@Test
	@Transactional
	void 결제완료시_PayementComplateEvent가_올바르게_발행되어진다() {
		User user = userJpaRepository.save(User.builder().name("철수").build());
		balanceJpaRepository.save(
			Balance.create(MoneyVO.create(BigDecimal.valueOf(10000)), LocalDateTime.now(), user.getId()));

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
				.price(MoneyVO.create(BigDecimal.valueOf(5000)))
				.status(ConcertSeatStatus.AVAILABLE)
				.build());
		// 3) 예약(HELD 상태)
		Reservation reservation = reservationJpaRepository.save(
			Reservation.createPendingReservation(
				user, seat, schedule, ReservationStatus.HELD));

		// 5) 결제 명령
		PaymentCommand command = new PaymentCommand(reservation.getId(), BigDecimal.valueOf(5000));
		/*-------- act -------- */

		PaymentInfo info = paymentService.payment(
			reservation.getId(), user.getId(), command);

		// 이벤트 발행을 확인한다.
		List<PaymentCompletedEvent> events = applicationEvents.stream(PaymentCompletedEvent.class).toList();

		// 2. 이벤트 개수 확인
		assertThat(events).hasSize(1);
		// 3. 이벤트 내용 확인
		PaymentCompletedEvent event = events.get(0);
		assertThat(event.scheduleId()).isEqualTo(schedule.getId());
		assertThat(event.concertOpenDate()).isEqualTo(schedule.getConcertOpenDate());
		assertThat(event.concertDate()).isEqualTo(schedule.getConcertDate());
		assertThat(event.concertTitle()).isEqualTo(concert.getConcertTitle());
		assertThat(event.paymentId()).isEqualTo(1L);
		assertThat(event.userId()).isEqualTo(user.getId());
		assertThat(event.money()).isEqualTo(seat.getPrice().getAmount());

		// 5. 결제 결과 확인 (부수 효과)
		assertThat(info).isNotNull();
	}
}
