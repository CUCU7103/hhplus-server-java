package kr.hhplus.be.server.application.reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional
	public ReservationInfo reservationSeat(long seatId, long userId, ReservationCommand command) {
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
		Reservation reservation = reservationRepository.save(
			Reservation.createPendingReservation(user, seat, concertSchedule, ReservationStatus.HELD));

		return ReservationInfo.from(reservation);
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
