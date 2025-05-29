package kr.hhplus.be.server.global.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.concert.CalculateTime;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistoryRepository;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingRepository;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.event.RankContext;
import kr.hhplus.be.server.domain.payment.message.PaymentMessageProducer;
import kr.hhplus.be.server.presentation.payment.PaymentMessageConsumer;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerUnitTest {

	@Mock
	private PaymentMessageProducer paymentMessageProducer;

	@Mock
	private ConcertRankingRepository concertRankingRepository;

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private ConcertRankingHistoryRepository concertRankingHistoryRepository;

	@Mock
	private CalculateTime processor;

	@InjectMocks
	private PaymentMessageConsumer paymentMessageConsumer;

	@Test
	void rankingUpdateListener_예외발생시_DLT전송() {
		// arrange
		PaymentCompletedEvent testEvent = PaymentCompletedEvent.create(
			1L,
			"테스트 콘서트",
			LocalDate.of(2025, 6, 10),
			LocalDateTime.of(2025, 6, 20, 10, 0),
			1L,
			1L,
			BigDecimal.valueOf(1000L),
			LocalDateTime.now()
		);
		given(concertRepository.getAvailableConcertSeat(testEvent.scheduleId()))
			.willThrow(new RuntimeException("DB 연결 실패"));

		// act - 예외가 내부에서 catch되므로 메서드 호출 자체는 성공
		assertThatCode(() -> {
			paymentMessageConsumer.rankingUpdateListener(testEvent);
		}).doesNotThrowAnyException();

		// assert - DLT 전송이 한 번 호출되어야 함
		then(paymentMessageProducer)
			.should(times(1))
			.sendDlt("payment-completed-dlt", testEvent);
	}

	@Test
	void rankingUpdateListener_Redis저장실패시_DLT전송() {
		// arrange
		PaymentCompletedEvent testEvent = PaymentCompletedEvent.create(
			1L,
			"테스트 콘서트",
			LocalDate.of(2025, 6, 10),
			LocalDateTime.of(2025, 6, 20, 10, 0),
			1L,
			1L,
			BigDecimal.valueOf(1000L),
			LocalDateTime.now()
		);

		given(concertRepository.getAvailableConcertSeat(testEvent.scheduleId()))
			.willReturn(0L); // 매진 상황

		given(processor.calculateMillis(testEvent.concertOpenDate()))
			.willReturn(3600000L);

		given(concertRankingRepository.saveSelloutTime(any(RankContext.class), eq(3600000L)))
			.willThrow(new RuntimeException("Redis 연결 실패"));

		// act - 예외가 내부에서 처리되므로 예외가 전파되지 않음
		assertThatCode(() -> {
			paymentMessageConsumer.rankingUpdateListener(testEvent);
		}).doesNotThrowAnyException();

		// assert - DLT 전송 검증
		then(paymentMessageProducer)
			.should(times(1))
			.sendDlt("payment-completed-dlt", testEvent);

		// Redis 저장 시도는 한 번 이루어져야 함
		then(concertRankingRepository)
			.should(times(1))
			.saveSelloutTime(any(RankContext.class), eq(3600000L));
	}
}
