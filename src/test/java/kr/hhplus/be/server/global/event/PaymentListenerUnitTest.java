package kr.hhplus.be.server.global.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.rank.RankingHistory;
import kr.hhplus.be.server.domain.rank.RankingHistoryRepository;
import kr.hhplus.be.server.global.support.event.PaymentEventPublisher;
import kr.hhplus.be.server.global.support.event.PaymentListener;
import kr.hhplus.be.server.global.support.event.SearchRankListenerContext;
import kr.hhplus.be.server.global.support.event.SelloutProcessor;

@ExtendWith(MockitoExtension.class)
public class PaymentListenerUnitTest {

	@Mock
	private ConcertRankRepository concertRankRepository;

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private RankingHistoryRepository rankingHistoryRepository;

	@Mock
	private Clock clock;

	@Mock
	private SelloutProcessor processor;

	@Mock
	private PaymentEventPublisher paymentEventPublisher;

	@InjectMocks
	private PaymentListener paymentListener;

	@Test
	void 좌석이_매진_상태가_아니면_랭킹_저장을_수행하지_않습니다() {
		// arrange
		long scheduleId = 1L;
		given(paymentEventPublisher.getScheduleId()).willReturn(scheduleId);
		given(concertRepository.getAvailableConcertSeat(scheduleId)).willReturn(100L);
		// act
		paymentListener.checkedSellout(paymentEventPublisher);

		// assert
		then(concertRankRepository).shouldHaveNoInteractions();
		then(rankingHistoryRepository).shouldHaveNoInteractions();
		then(processor).shouldHaveNoInteractions();
	}

	@Test
	void 매진인_경우_redis_저장에_실패하면_백업이_실행됨() {
		// given
		long scheduleId = 1L;
		long millis = 1000L;

		SearchRankListenerContext context = new SearchRankListenerContext("콘서트1", "2025-12-01");
		RankingHistory expectedBackupData = RankingHistory.createBackup(
			context.concertName(),
			LocalDate.parse(context.concertDate()),
			millis
		);

		given(paymentEventPublisher.getScheduleId()).willReturn(scheduleId);
		given(concertRepository.getAvailableConcertSeat(scheduleId)).willReturn(0L); // 매진됨
		given(processor.calculateMillis(scheduleId)).willReturn(millis);
		given(processor.buildContext(scheduleId)).willReturn(context);
		given(concertRankRepository.saveSelloutTime(context, millis)).willReturn(false); // Redis 저장 실패

		// when
		paymentListener.checkedSellout(paymentEventPublisher);

		// then
		verify(processor, times(1)).calculateMillis(scheduleId);
		verify(processor, times(1)).buildContext(scheduleId);
		verify(concertRankRepository, times(1)).saveSelloutTime(context, millis);

		// RankingHistory 객체의 생성 및 저장 검증
		// Mockito의 ArgumentCaptor를 사용하여 실제 저장된 객체 캡처 및 검증
		ArgumentCaptor<RankingHistory> historyCaptor = ArgumentCaptor.forClass(RankingHistory.class);
		verify(rankingHistoryRepository, times(1)).saveBackup(historyCaptor.capture());

	/*	// then
		then(processor).should(times(1)).calculateMillis(scheduleId);
		then(processor).should(times(1)).buildContext(scheduleId);
		then(concertRankRepository).should(times(1)).saveSelloutTime(context, millis);

		// RankingHistory 객체의 생성 및 저장 검증
		// Mockito의 ArgumentCaptor를 사용하여 실제 저장된 객체 캡처 및 검증
		ArgumentCaptor<RankingHistory> historyCaptor = ArgumentCaptor.forClass(RankingHistory.class);
		then(rankingHistoryRepository).should(times(1)).saveBackup(historyCaptor.capture());*/

		// 캡처된 객체 검증
		RankingHistory capturedHistory = historyCaptor.getValue();
		assertThat(capturedHistory).isNotNull();
		// 필요시 추가 필드 검증 (equals 메소드 구현 여부에 따라 달라질 수 있음)
		// assertThat(capturedHistory).isEqualToComparingFieldByField(expectedBackupData);
	}
}
