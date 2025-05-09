package kr.hhplus.be.server.application.reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.global.support.lock.model.LockType;
import kr.hhplus.be.server.global.support.lock.model.WithLock;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ReservationRepository reservationRepository;
	private final ConcertRepository concertRepository;
	private final UserRepository userRepository;

	/**
	 * 토큰 검증은 별도의 인터셉터에서 사용
	 * 유효한 콘서트 스케줄(날짜인지 확인)
	 * 유효한 스케줄인 경우
	 * 좌석의 상태가 예약가능한지 확인
	 * 유효하지 않은 스케줄일 경우
	 * 예외를 발생시킨다.
	 */

	/**
	 * 낙관적 락을 걸거다
	 *
	 * */
	@WithLock(
		key = "seat:reserver",
		type = LockType.REDIS_SPIN, timeoutMillis = 4000,
		retryIntervalMillis = 200,
		expireMillis = 5000
	)
	@Transactional
	public ReservationInfo reserve(long seatId, long userId, ReservationCommand command) {
		// 유효한 스케줄인지 확인
		ConcertSchedule concertSchedule = concertRepository.getConcertSchedule(command.concertScheduleId(),
				command.concertScheduleDate())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));
		// 좌석 조회
		ConcertSeat seat = concertRepository
			.getConcertSeatWhere(seatId, command.concertScheduleId(),
				command.concertScheduleDate(),
				ConcertSeatStatus.AVAILABLE)
			.orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_RESERVATION_CONCERT_SEAT));
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
		// 예약 정보 생성
		// saveFlush
		// 낙관적 락을 검출하기 위해 save 한 점도 좋았어요. 여기서 주의하셔야할 점은, 만약 낙관적 락이 트랜잭션 범위 도중에 존재한다면, flush 를 명시적으로 해야한다는 점입니다
		// 결제의 경우, 말씀하신 것처럼 강결합이 우려될 수 있는데요. 이럴 경우엔, 같은 트랜잭션 내의 파사드에서 말씀하신 협력객체의 로직을 수행하고,
		// 필요한 정보만 paymentService#create 에 넘겨 ( 예를 들면, orderId, userId 등등 ) 생성 로직은 가볍게 만드는 방법을 취할 수도 있습니다.
		// 예외를 잡기 위함
		try {
			Reservation reservation = reservationRepository
				.saveAndFlush(
					Reservation.createPendingReservation(user, seat, concertSchedule, ReservationStatus.HELD));
			return ReservationInfo.from(reservation);
		} catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
			throw new CustomException(CustomErrorCode.FAILED_RESERVATION_SEAT);
		} catch (Exception e) {
			throw new CustomException(CustomErrorCode.SERVER_ERROR);
		}
	}

	@Transactional
	public void concertReservationCancel() {
		List<Reservation> reservations = reservationRepository
			.getConcertReservationStatus(ReservationStatus.HELD);
		for (Reservation reservation : reservations) {
			reservation.cancelDueToTimeout(LocalDateTime.now());
		}
	}

}
