package kr.hhplus.be.server.application.integration;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

}
