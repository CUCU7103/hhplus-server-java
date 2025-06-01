package kr.hhplus.be.server.presentation.payment;

import java.time.LocalDate;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.concert.CalculateTime;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistory;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistoryRepository;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingRepository;
import kr.hhplus.be.server.domain.payment.event.MessageContext;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.event.RankContext;
import kr.hhplus.be.server.domain.payment.message.PaymentMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageConsumer {

	private final ConcertRankingRepository concertRankingRepository;
	private final ConcertRepository concertRepository;
	private final ConcertRankingHistoryRepository concertRankingHistoryRepository;
	private final PaymentMessageProducer paymentMessageProducer;
	private final CalculateTime processor;

	@KafkaListener(topics = "payment-completed", groupId = "ranking")
	public void rankingUpdateListener(PaymentCompletedEvent event) {
		try {
			// 재시도 로직이 포함된 별도 메서드 호출
			processRankingUpdate(event);
		} catch (Exception e) {
			log.error("최종 실패—DLT로 보냅니다: {}", event, e);
			paymentMessageProducer.sendDlt("payment-completed-dlt", event);
		}
	}

	/**
	 * 비즈니스 로직을 별도 메서드로 분리하여 @Retryable 적용
	 */
	@Retryable(
		value = Exception.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 100)
	)
	public void processRankingUpdate(PaymentCompletedEvent event) {
		// (1) 잔여 좌석 확인
		long availableSeats = concertRepository.getAvailableConcertSeat(event.scheduleId());
		if (availableSeats != 0) {
			log.info("아직 잔여 좌석이 있습니다", availableSeats);
			return;
		}

		// (2) 매진 시간 계산
		long millis = processor.calculateMillis(event.concertOpenDate());

		// (3) Redis에 저장
		RankContext context = RankContext.of(event.concertTitle(), event.concertDate());
		log.info("Redis에 랭킹 저장 시도…");
		boolean result = concertRankingRepository.saveSelloutTime(context, millis);

		// (4) Redis 저장 실패 시 DB 백업
		if (!result) {
			log.warn("Redis 저장 실패, DB 백업으로 폴백 처리합니다");
			concertRankingHistoryRepository.saveBackup(
				ConcertRankingHistory.createBackup(
					context.concertName(),
					LocalDate.parse(context.concertDate()),
					millis
				)
			);
			log.info("DB 백업 완료");
		}

		log.info("랭킹 저장 로직 완료 ");
	}

	@Recover
	public void recover(Exception e, PaymentCompletedEvent event) {
		log.error("3번 재시도에도 실패: {}", e.getMessage());
		// @Recover에서는 예외를 다시 던져서 상위 try-catch에서 DLT 처리하도록 함
		throw new RuntimeException("재시도 한도 초과", e);
	}

	@KafkaListener(topics = "payment-completed", groupId = "message")
	public void sentMessageListener(PaymentCompletedEvent event) throws InterruptedException {
		// 결제 정보 전달하기
		log.info("알림 메시지 발송 중");
		Thread.sleep(2000L);
		MessageContext context = MessageContext.of(event);
		log.info("message info {} ", context);
		log.info("알림 메시지 발송완료");

	}

}
