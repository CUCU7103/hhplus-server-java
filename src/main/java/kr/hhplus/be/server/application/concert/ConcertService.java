package kr.hhplus.be.server.application.concert;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.benmanes.caffeine.cache.Cache;

import kr.hhplus.be.server.application.concert.command.ConcertDateSearchCommand;
import kr.hhplus.be.server.application.concert.command.ConcertSeatSearchCommand;
import kr.hhplus.be.server.application.concert.info.ConcertScheduleInfo;
import kr.hhplus.be.server.application.concert.info.ConcertSeatInfo;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleCashRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.global.support.page.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConcertService {

	/**
	 * 먼저 콘서트 서비스에  콘서트 스케줄, 콘서트 관련 로직들을 전부 몰아서 넣고 많은 것 같으면 분리 진행하기
	 */
	private final ConcertRepository concertRepository;
	private final ConcertScheduleCashRepository cacheRepository;
	private final Cache<String, List<ConcertScheduleInfo>> localCache;

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
		String key = concertId + "::schedule::" + command.startDate() + "::" + command.endDate();

		// 1) L1: Caffeine 로컬 캐시 조회
		List<ConcertScheduleInfo> fromLocal = localCache.getIfPresent(key);
		if (fromLocal != null) {
			log.info("[Local Cache HIT]");
			log.info("[LocalCache] list size {}", fromLocal.size());
			return PaginationUtils.getPage(fromLocal, command.page(), command.size());
		}
		log.info("[Cache MISS] REDIS 조회 실행");

		// 2) L2: Redis 원격 캐시 조회 - 예외 처리 추가
		ConcertScheduleInfo[] fromRedis = null;
		try {
			fromRedis = cacheRepository.get(key, ConcertScheduleInfo[].class);
		} catch (Exception e) {
			// Redis 접근 과정에서 발생하는 모든 예외 로깅 후 진행 (circuit breaker 패턴)
			log.error("[Redis Cache 조회 실패] key={}, 오류={}", key, e.getMessage(), e);
			// Redis 실패 시 바로 DB 조회로 진행
		}

		if (fromRedis != null) {
			List<ConcertScheduleInfo> redisList = Arrays.asList(fromRedis);
			log.info("[Redis Cache HIT]");
			log.info("[Redis Cache] list size {}", redisList.size());
			// 로컬 캐시에 워밍업
			try {
				localCache.put(key, redisList);
			} catch (Exception e) {
				log.error("[Local Cache 저장 실패] key={}, 오류={}", key, e.getMessage(), e);
			}
			return PaginationUtils.getPage(redisList, command.page(), command.size());
		}

		// 3) DB 조회
		log.info("[Cache MISS] DB 조회 실행");
		concertRepository.findByConcertId(concertId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		List<ConcertSchedule> concertSchedules = concertRepository
			.getConcertScheduleListOrderByDate(
				concertId,
				command.startDate(), command.endDate(),
				ConcertScheduleStatus.AVAILABLE,
				Sort.by("concertDate").descending()
			);

		List<ConcertScheduleInfo> allResults = concertSchedules.stream()
			.map(ConcertScheduleInfo::from)
			.toList();

		// 4) L2: Redis에 저장 - 예외 처리 추가
		try {
			cacheRepository.put(key, allResults, 3L);
			log.info("[Redis Cache SAVE] key={}", key);
		} catch (Exception e) {
			// Redis 저장 실패 시에도 서비스는 계속 진행
			log.error("[Redis Cache 저장 실패] key={}, 오류={}", key, e.getMessage(), e);
		}

		// 5) L1: 로컬 캐시에 저장
		try {
			localCache.put(key, allResults);
			log.info("[Local Cache SAVE] key={}", key);
		} catch (Exception e) {
			// 로컬 캐시 저장 실패 시에도 서비스는 계속 진행
			log.error("[Local Cache 저장 실패] key={}, 오류={}", key, e.getMessage(), e);
		}

		return PaginationUtils.getPage(allResults, command.page(), command.size());
	}

/*	@Transactional(readOnly = true)
	public List<ConcertScheduleInfo> searchDate(long concertId, ConcertDateSearchCommand command) {
		// DB 조회
		concertRepository.findByConcertId(concertId)
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		List<ConcertSchedule> concertSchedules = concertRepository
			.getConcertScheduleListOrderByDate(
				concertId,
				command.startDate(), command.endDate(),
				ConcertScheduleStatus.AVAILABLE,
				Sort.by("concertDate").descending()
			);

		List<ConcertScheduleInfo> allResults = concertSchedules.stream()
			.map(ConcertScheduleInfo::from)
			.toList();

		return PaginationUtils.getPage(allResults, command.page(), command.size());
	}*/

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
	public List<ConcertSeatInfo> searchSeat(long concertScheduleId, ConcertSeatSearchCommand command) {

		ConcertSchedule concertSchedule = concertRepository.getConcertSchedule(concertScheduleId, command.concertDate())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));

		concertRepository.findByConcertId(concertSchedule.getConcert().getId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		Pageable pageable = PageRequest.of(command.page(), command.size(), Sort.by("section"));

		Page<ConcertSeat> seats = concertRepository.findByConcertScheduleIdAndSeatStatusContaining(concertScheduleId,
			ConcertSeatStatus.AVAILABLE, pageable);
		log.info("test {} ", seats.stream().toList());
		return seats.stream().map(ConcertSeatInfo::from).toList();

	}

}
