package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import kr.hhplus.be.server.domain.concert.CalculateTime;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistory;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistoryRepository;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingRepository;
import kr.hhplus.be.server.domain.payment.event.MessageContext;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.event.RankContext;
import kr.hhplus.be.server.global.event.model.PaymentCompleteEventFixture;
import kr.hhplus.be.server.presentation.concert.ConcertEventListener;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
public class ConcertEventListenerUnitTest {

	@Mock
	private ConcertRankingRepository concertRankingRepository;

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private ConcertRankingHistoryRepository concertRankingHistoryRepository;

	@Mock
	private CalculateTime processor;

	@InjectMocks
	private ConcertEventListener concertEventListener;

	@Test
	void 좌석이_매진_상태가_아니면_랭킹_저장을_수행하지_않습니다() {
		// arrange
		long scheduleId = 1L;
		LocalDateTime concertOpenDate = LocalDateTime.of(2025, 6, 10, 10, 00);
		LocalDate concertDate = LocalDate.of(2025, 5, 10);
		PaymentCompletedEvent event = PaymentCompleteEventFixture.createForRankingTest(scheduleId, "윤하 콘서트",
			concertDate, concertOpenDate);

		given(concertRepository.getAvailableConcertSeat(scheduleId)).willReturn(100L);
		// act
		concertEventListener.rankingUpdateListener(event);

		// assert
		then(concertRankingRepository).shouldHaveNoInteractions();
		then(concertRankingHistoryRepository).shouldHaveNoInteractions();
		then(processor).shouldHaveNoInteractions();
	}

	@Test
	void 매진인_경우_redis_저장에_실패하면_백업이_실행됨() {
		// given
		long scheduleId = 1L;
		long millis = 1000L;

		LocalDateTime concertOpenDate = LocalDateTime.of(2025, 6, 10, 10, 00);
		LocalDate concertDate = LocalDate.of(2025, 5, 10);

		PaymentCompletedEvent event = PaymentCompleteEventFixture.createForRankingTest(scheduleId, "윤하 콘서트",
			concertDate, concertOpenDate);
		RankContext context = RankContext.of(event.concertTitle(), event.concertDate());
		ConcertRankingHistory expectedBackupData = ConcertRankingHistory.createBackup(
			context.concertName(),
			LocalDate.parse(context.concertDate()),
			millis
		);

		given(concertRepository.getAvailableConcertSeat(scheduleId)).willReturn(0L); // 매진됨
		given(processor.calculateMillis(concertOpenDate)).willReturn(millis);
		given(concertRankingRepository.saveSelloutTime(context, millis)).willReturn(false); // Redis 저장 실패

		// when
		concertEventListener.rankingUpdateListener(event);

		// then
		verify(processor, times(1)).calculateMillis(concertOpenDate);
		verify(concertRankingRepository, times(1)).saveSelloutTime(context, millis);

		// RankingHistory 객체의 생성 및 저장 검증
		// Mockito의 ArgumentCaptor를 사용하여 실제 저장된 객체 캡처 및 검증
		ArgumentCaptor<ConcertRankingHistory> historyCaptor = ArgumentCaptor.forClass(ConcertRankingHistory.class);
		verify(concertRankingHistoryRepository, times(1)).saveBackup(historyCaptor.capture());

		// then
		then(processor).should(times(1)).calculateMillis(concertOpenDate);
		then(concertRankingRepository).should(times(1)).saveSelloutTime(context, millis);

		// 캡처된 객체 검증
		ConcertRankingHistory capturedHistory = historyCaptor.getValue();
		assertThat(capturedHistory).isNotNull();
		// 필요시 추가 필드 검증 (equals 메소드 구현 여부에 따라 달라질 수 있음)
		// assertThat(capturedHistory).isEqualToComparingFieldByField(expectedBackupData);
	}

	@Test
	void 결제_후_메시지_전송_이벤트_로그_검증(CapturedOutput output) throws InterruptedException {
		// arrange
		PaymentCompletedEvent paymentCompletedEvent = PaymentCompleteEventFixture.createForMessageTest(1L, 1L,
			BigDecimal.valueOf(1000), LocalDateTime.now());
		MessageContext context = MessageContext.of(paymentCompletedEvent);
		// act
		concertEventListener.sentMessageListener(paymentCompletedEvent);
		// assert
		String logs = output.getOut();
		assertThat(logs)
			.contains("알림 메시지 발송 중")
			.contains("message info " + context)
			.contains("알림 메시지 발송완료");

	}

}
