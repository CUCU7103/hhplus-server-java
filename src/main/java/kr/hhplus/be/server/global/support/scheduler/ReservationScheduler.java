package kr.hhplus.be.server.global.support.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.application.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservationScheduler {

	private final ReservationService reservationService;

	// 5분 주기로 스케줄러 실행
	@Scheduled(cron = "0 */5 * * * *")
	public void checkAndReleaseHeldSeats() {
		log.info("좌석 상태 확인 스케줄러 실행: {}", java.time.LocalDateTime.now());
		reservationService.concertReservationCancel();
		log.info("스케줄러 실행 완료. 다음 실행은 5분 후입니다.");
	}
}
