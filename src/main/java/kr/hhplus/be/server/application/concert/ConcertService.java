package kr.hhplus.be.server.application.concert;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.concert.command.ConcertDateSearchCommand;
import kr.hhplus.be.server.application.concert.command.ConcertSeatSearchCommand;
import kr.hhplus.be.server.application.concert.info.ConcertScheduleInfo;
import kr.hhplus.be.server.application.concert.info.ConcertSeatInfo;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.infrastructure.concert.ConcertDomainRepositoryImpl;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertService {

	/**
	 * 먼저 콘서트 서비스에  콘서트 스케줄, 콘서트 관련 로직들을 전부 몰아서 넣고 많은 것 같으면 분리 진행하기
	 */
	private final ConcertDomainRepositoryImpl concertDomainRepositoryImpl;

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
	public List<ConcertScheduleInfo> searchDate(long concertId, ConcertDateSearchCommand command) {

		concertDomainRepositoryImpl.findByConcertId(concertId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		List<ConcertSchedule> concertSchedules = concertDomainRepositoryImpl.getConcertScheduleList(concertId,
			command.startDate(), command.endDate(), ConcertScheduleStatus.AVAILABLE);

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
	public List<ConcertSeatInfo> searchSeat(long concertId, ConcertSeatSearchCommand command) {

		concertDomainRepositoryImpl.findByConcertId(concertId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		concertDomainRepositoryImpl.getConcertSchedule(command.concertScheduleId(),
				command.concertDate())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));

		Pageable pageable = PageRequest.of(command.page(), command.size(), Sort.by("section"));

		Page<ConcertSeat> seats = concertDomainRepositoryImpl.findByConcertScheduleIdAndSeatStatusContaining(
			command.concertScheduleId(),
			ConcertSeatStatus.AVAILABLE, pageable);

		return seats.stream()
			.map(ConcertSeatInfo::from)
			.toList();

	}

	/**
	 *  스케줄러 로직
	 *  먼저 예약 도메인의 임시예약 상태를 조회한다.
	 *  임시예약 상태인 좌석들의 만료 시간을 확인한다
	 *  현재시간 보다 만료시간이 이전인 예약의 상태를 예약 가능 상태로 변경한다.
	 *  좌석의 상태도 예약 가능 상태로 변경한다.
	 */

}
