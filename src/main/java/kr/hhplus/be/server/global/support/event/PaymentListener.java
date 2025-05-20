package kr.hhplus.be.server.global.support.event;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.rank.RankingHistory;
import kr.hhplus.be.server.domain.rank.RankingHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {
	private final ConcertRankRepository concertRankRepository;
	private final ConcertRepository concertRepository;
	private final RankingHistoryRepository rankingHistoryRepository;
	private final Clock clock;
	private final SelloutProcessor processor;

	@Async  // 비동기 처리를 위한 어노테이션 추가
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void checkedSellout(PaymentEventPublisher event) {
		try {
			ZoneId zone = ZoneId.systemDefault();
			long concertScheduleId = event.getScheduleId();
			long availableSeats = concertRepository.getAvailableConcertSeat(concertScheduleId);

			if (availableSeats != 0) {
				log.info("매진이 아니기에 랭킹 저장을 수행하지 않습니다");
				return;
			}
			// 매진 시간 계산
			long millis = processor.calculateMillis(concertScheduleId);
			// redis에 저장할 context 객체 생성
			SearchRankListenerContext context = processor.buildContext(concertScheduleId);
			// redis 저장 수행
			log.info("Redis에 랭킹 저장 로직 수행");
			boolean result = concertRankRepository.saveSelloutTime(context, millis);
			// 저장 실패시 백업 진행
			if (!result) {
				log.info("DB 저장로직 수행완료");
				rankingHistoryRepository.saveBackup(RankingHistory.createBackup(
					context.concertName(),
					LocalDate.parse(context.concertDate()),
					millis
				));
			}
			log.info("Redis에 저장 성공");
		} catch (Exception e) {
			log.error("이벤트 처리중 예외가 발생하였습니다 {}", e.getMessage());
			throw e;
		}

	}

}
