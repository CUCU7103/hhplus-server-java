package kr.hhplus.be.server.presentation.payment;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.payment.event.CalculateTime;
import kr.hhplus.be.server.domain.payment.event.MessageContext;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.event.RankContext;
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
	private final CalculateTime processor;

	@Async  // 비동기 처리를 위한 어노테이션 추가
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void rankingUpdateListener(PaymentCompletedEvent event) {
		ZoneId zone = ZoneId.systemDefault();
		long availableSeats = concertRepository.getAvailableConcertSeat(event.scheduleId());

		if (availableSeats != 0) {
			log.info("매진이 아니기에 랭킹 저장을 수행하지 않습니다");
			return;
		}
		// 매진 시간 계산
		long millis = processor.calculateMillis(event.concertOpenDate());
		// redis에 저장할 context 객체 생성
		RankContext context = RankContext.of(event.concertTitle(), event.concertDate());
		// redis 저장 수행
		log.info("Redis에 랭킹 저장 로직 수행");
		boolean result = concertRankRepository.saveSelloutTime(context, millis);
		// 저장 실패시 백업 진행
		if (!result) {
			log.info("DB 저장로직 수행");
			rankingHistoryRepository.saveBackup(RankingHistory.createBackup(
				context.concertName(),
				LocalDate.parse(context.concertDate()),
				millis
			));
			log.info("DB 저장로직 수행완료");
		}
		log.info("Redis에 저장 성공");

	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void sentMessageListener(PaymentCompletedEvent event) throws InterruptedException {
		// 결제 정보 전달하기
		log.info("알림 메시지 발송 중");
		Thread.sleep(2000L);
		MessageContext context = MessageContext.of(event);
		log.info("message info {} ", context);
		log.info("알림 메시지 발송완료");

	}

}
