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
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleCashRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.payment.event.RankContext;
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
	private final ConcertRankingRepository rankRepository;

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

	@Transactional(readOnly = true)
	public List<ConcertSeatInfo> searchSeat(long concertScheduleId, ConcertSeatSearchCommand command) {

		ConcertSchedule concertSchedule = concertRepository.getConcertScheduleWithDate(concertScheduleId,
				command.concertDate())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_SCHEDULE));

		concertRepository.findByConcertId(concertSchedule.getConcert().getId())
			.orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_CONCERT));

		Pageable pageable = PageRequest.of(command.page(), command.size(), Sort.by("section"));

		Page<ConcertSeat> seats = concertRepository.findByConcertScheduleIdAndSeatStatusContaining(concertScheduleId,
			ConcertSeatStatus.AVAILABLE, pageable);
		log.info("test {} ", seats.stream().toList());
		return seats.stream().map(ConcertSeatInfo::from).toList();

	}

	@Transactional(readOnly = true)
	public List<RankContext> top5ConcertSchedule() {
		return rankRepository.top5ConcertSchedule().stream().toList();
	}

}
