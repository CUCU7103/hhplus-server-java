package kr.hhplus.be.server.interfaces;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.application.ConcertService;
import kr.hhplus.be.server.interfaces.scheduler.ConcertReservationScheduler;

@ExtendWith(MockitoExtension.class)
public class ConcertReservationSchedulerUnitTest {

	@Mock
	private ConcertService concertService;

	@InjectMocks
	private ConcertReservationScheduler scheduler;

	@Test
	public void testCheckAndReleaseHeldSeats() {
		// 실행 시 현재 시간 출력 및 내부에서 concertService.concertReservationCancel() 호출
		scheduler.checkAndReleaseHeldSeats();
		// concertService의 메서드가 정확히 1회 호출되었는지 검증
		verify(concertService, times(1)).concertReservationCancel();
	}
}
