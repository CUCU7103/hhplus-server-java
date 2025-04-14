package kr.hhplus.be.server.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.balance.Balance;
import kr.hhplus.be.server.domain.balance.model.BalanceRepository;
import kr.hhplus.be.server.domain.concert.ConcertPayment;
import kr.hhplus.be.server.domain.concert.ConcertReservation;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.info.ConcertPaymentInfo;
import kr.hhplus.be.server.domain.concert.info.ConcertReservationInfo;
import kr.hhplus.be.server.domain.concert.info.ConcertScheduleInfo;
import kr.hhplus.be.server.domain.concert.info.ConcertSeatInfo;
import kr.hhplus.be.server.domain.concert.model.ConcertRepository;
import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.concert.request.ConcertDateSearchRequest;
import kr.hhplus.be.server.interfaces.concert.request.ConcertPaymentRequest;
import kr.hhplus.be.server.interfaces.concert.request.ConcertReservationRequest;
import kr.hhplus.be.server.interfaces.concert.request.ConcertSeatSearchRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertService {

	/**
	 * 먼저 콘서트 서비스에  콘서트 스케줄, 콘서트 관련 로직들을 전부 몰아서 넣고 많은 것 같으면 분리 진행하기
	 */
	private final ConcertRepository concertRepository;
	private final UserRepository userRepository;
	private final BalanceRepository balanceRepository;
	private final TokenRepository tokenRepository;

	/**
	 * 	예약 가능 일자 조회 기능 <br/>
	 * 	예약 가능한 날짜 목록을 조회할 수 있습니다. ->
	 * 	결국 콘서트 스케줄을 조회한다는 의미, "예약 가능"이라는 조건이 붙은걸로 미루어보아 콘서트 스케줄 중 좌석이 모두 예약 상태인 스케줄은 조회가 안되도록 조건을 걸면 된다고 판단.<br/>
	 * 	<br> 요구사항 <br/>
	 *  해당하는 콘서트 스케줄 일정을 모두 조회한다.
	 *  콘서트 스케줄 중 좌석이 모두 예약 상태인 스케줄은 조회가 안되도록 처리한다.<br/>
	 *  즉 콘서트 스케줄에 상태를 부여한다.<br/>
	 * 	전달받은 콘서트 스케줄 아이디와 날짜값을 조회하고 없으면 빈 리스트 반환.<br/>
	 * <br>검증 조건<br/>
	 * 	유효한 스케줄인지 확인해야 한다.<br/>
	 * 		-> 아이디와 날짜값을 받고, 실제로 스케줄이 존재하는지 확인 , 없으면 예외처리<br/>
	 * 	유효한 날짜인지 확인해야 한다.<br/>
	 * 	-> 컨트롤러 부분에서 처리해야함. ->전달받은 날짜값이 현재일 이전인지 확인 필요
	 */
	@Transactional(readOnly = true)
	public List<ConcertScheduleInfo> searchDate(long concertId, ConcertDateSearchRequest request) {

		concertRepository.findByConcertId(concertId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		List<ConcertSchedule> concertSchedules = concertRepository.getConcertScheduleList(
			concertId,
			request.toCommand().startDate(), request.toCommand().endDate(), ConcertScheduleStatus.AVAILABLE);

		return concertSchedules.stream().map(ConcertScheduleInfo::from).toList();
	}

	/**
	 * 	예약가능 좌석 조회 기능 <br/>
	 *	요구사항
	 *  예약가능한 좌석의 숫자는 50개 입니다.
	 *  예약가능한 좌석의 상태는 AVAILABLE
	 *  사용자는 콘서트 ,콘서트 스케줄 아이디와, 날짜값을 가지고 예약 가능한 좌석을 조회한다.
	 *  좌석의 상태가 예약 가능 상태인 좌석만 조회하면 되지 않을까?
	 *  예약 가능한 좌석의 상태를 조회하기 위해서는 먼저 예약 가능 날짜를 선택하고
	 *  스케줄 아이디를 받아서 좌석을 조회하면 된다.
	 *  즉 콘서트 스케줄 아이디를 사용해서 좌석으로 들어간 다음에 좌석의 상태가 사용가능한 좌석인지를 확인하면 되는것.
	 */

	@Transactional(readOnly = true)
	public List<ConcertSeatInfo> searchSeat(long concertId, ConcertSeatSearchRequest request) {

		concertRepository.findByConcertId(concertId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		concertRepository.getConcertSchedule(request.toCommand().concertScheduleId(),
				request.toCommand().concertDate())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));

		Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by("section"));

		Page<ConcertSeat> seats = concertRepository.findByConcertScheduleIdAndSeatStatusContaining(
			request.concertScheduleId(),
			ConcertSeatStatus.AVAILABLE, pageable);

		return seats.stream()
			.map(ConcertSeatInfo::from)
			.toList();

	}

	/**
	 * 토큰 검증은 별도의 인터셉터에서 사용
	 * 유효한 콘서트 스케줄(날짜인지 확인)
	 * 유효한 스케줄인 경우
	 * 좌석의 상태가 예약가능한지 확인
	 * 유효하지 않은 스케줄일 경우
	 * 예외를 발생시킨다.
	 */
	@Transactional
	public ConcertReservationInfo reservationSeat(long seatId, ConcertReservationRequest request) {

		// 유효한 스케줄인지 확인
		ConcertSchedule concertSchedule = concertRepository.getConcertSchedule(request.toCommand().concertScheduleId(),
				request.toCommand().concertScheduleDate())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));
		// 좌석 조회
		ConcertSeat seat = concertRepository
			.getConcertSeatWhere(seatId, request.toCommand().concertScheduleId(),
				request.toCommand().concertScheduleDate(),
				ConcertSeatStatus.AVAILABLE)
			.orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_RESERVATION_CONCERT_SEAT));

		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		seat.changeStatus(ConcertSeatStatus.HELD); // 좌석상태 임시예약으로 변경

		// 예약정보 생성
		ConcertReservation reservation = concertRepository.save(
			ConcertReservation.createPendingReservation(user, seat, concertSchedule));

		return ConcertReservationInfo.from(reservation);
	}

	/**
	 *  사용자의 잔여 포인트를 조회한다.
	 *  잔여 포인트 조회 후 결제 여부를 판단한다.
	 *  결제 금액이 부족한 경우 예외처리
	 *  결제 금액이 충분한 경우
	 *  포인트 차감 후 저장
	 *  좌석 상태를 변경
	 *  좌석의 결제 정보를 저장
	 *  토큰의 상태를 만료 처리한다.
	 */
	@Transactional
	public ConcertPaymentInfo paymentSeat(long reservationId, ConcertPaymentRequest request) {
		Balance userbalance = balanceRepository.findById(request.toCommand().userId()).orElseThrow(
			() -> new CustomException(CustomErrorCode.NOT_FOUND_BALANCE));
		ConcertSeat seat = concertRepository.getByConcertSeatId(request.toCommand().seatId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT_SEAT));
		ConcertReservation reservation = concertRepository.getByConcertReservationId(reservationId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION));
		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

		// 결제 수행
		userbalance.usePoint(request.toCommand().amount());
		reservation.validateStatus();
		seat.validateStatus();
		seat.changeStatus(ConcertSeatStatus.BOOKED);

		ConcertPayment payment = concertRepository.save(ConcertPayment
			.createPayment(reservation, user, request.toCommand().amount()));

		Token token = tokenRepository.getToken(request.toCommand().userId());
		token.expiredToken();

		return ConcertPaymentInfo.from(payment);
	}

	/**
	 *  스케줄러 로직
	 *  먼저 예약 도메인의 임시예약 상태를 조회한다.
	 *  임시예약 상태인 좌석들의 만료 시간을 확인한다
	 *  현재시간 보다 만료시간이 이전인 예약의 상태를 예약 가능 상태로 변경한다.
	 *  좌석의 상태도 예약 가능 상태로 변경한다.
	 */
	@Transactional
	public void concertReservationCancel() {
		List<ConcertReservation> reservations = concertRepository
			.getConcertReservationStatus(ConcertReservationStatus.HELD);
		for (ConcertReservation reservation : reservations) {
			reservation.cancel(LocalDateTime.now());
		}
	}
}
