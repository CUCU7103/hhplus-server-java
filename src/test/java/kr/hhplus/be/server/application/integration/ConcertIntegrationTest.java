package kr.hhplus.be.server.application.integration;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.command.ConcertDateSearchCommand;
import kr.hhplus.be.server.application.concert.command.ConcertSeatSearchCommand;
import kr.hhplus.be.server.application.concert.info.ConcertScheduleInfo;
import kr.hhplus.be.server.application.concert.info.ConcertSeatInfo;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.infrastructure.concert.ConcertJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
public class ConcertIntegrationTest {

	@Autowired
	private ConcertService concertService;

	@Autowired
	private ConcertJpaRepository concertJpaRepository;
	@Autowired
	private ConcertScheduleJpaRepository concertScheduleJpaRepository;
	@Autowired
	private ConcertSeatJpaRepository concertSeatJpaRepository;

	@Test
	void 예외가_발생하지_않으면_콘서트_스케줄_조회에_성공한다() {
		// arrange
		Concert concert = concertJpaRepository.save(
			Concert.builder().concertTitle("윤하 콘서트").artistName("윤하").build());
		ConcertSchedule schedule1 = ConcertSchedule.builder()
			.concertDate(LocalDate.of(2025, 6, 20))
			.venue("성균관대학교")
			.status(
				ConcertScheduleStatus.AVAILABLE)
			.createdAt(LocalDateTime.now())
			.concert(concert)
			.build();
		ConcertSchedule schedule2 = ConcertSchedule.builder()
			.concertDate(LocalDate.of(2025, 7, 22))
			.venue("서울대학교")
			.status(
				ConcertScheduleStatus.AVAILABLE)
			.createdAt(LocalDateTime.now())
			.concert(concert)
			.build();
		ConcertSchedule schedule3 = ConcertSchedule.builder()
			.concertDate(LocalDate.of(2025, 8, 22))
			.venue("연세대학교")
			.status(
				ConcertScheduleStatus.AVAILABLE)
			.createdAt(LocalDateTime.now())
			.concert(concert)
			.build();
		List<ConcertSchedule> schedules = Arrays.asList(schedule1, schedule2, schedule3);
		concertScheduleJpaRepository.saveAll(schedules);

		ConcertDateSearchCommand command = new ConcertDateSearchCommand(LocalDate.of(2025, 5, 1),
			LocalDate.of(2025, 9, 22));

		// act
		List<ConcertScheduleInfo> info = concertService.searchDate(concert.getId(), command);
		// assert
		assertThat(info)
			.hasSize(3)
			.extracting("concertDate") // 또는 .extracting(ConcertScheduleInfo::getConcertDate)
			.containsExactlyInAnyOrder(
				LocalDate.of(2025, 6, 20),
				LocalDate.of(2025, 7, 22),
				LocalDate.of(2025, 8, 22)
			);
	}

	@Test
	void 예외가_발생하지_않으면_좌석_조회에_성공한다() {
		// arrange
		// 1) 콘서트 저장
		Concert concert = concertJpaRepository.save(
			Concert.builder()
				.concertTitle("윤하 콘서트")
				.artistName("윤하")
				.build()
		);

		// 2) 스케줄 저장
		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 5, 22))
				.venue("서울대학교")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build()
		);

		// 3) 좌석 저장 (AVAILABLE 상태)
		ConcertSeat seat1 = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("A")
				.seatNumber(1)
				.status(ConcertSeatStatus.AVAILABLE)
				.build()
		);
		ConcertSeat seat2 = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("B")
				.seatNumber(1)
				.status(ConcertSeatStatus.AVAILABLE)
				.build()
		);

		// 검색 조건
		ConcertSeatSearchCommand command = new ConcertSeatSearchCommand(
			schedule.getId(),          // concertScheduleId
			schedule.getConcertDate(), // concertDate
			0,                         // page
			5                          // size
		);

		// act
		List<ConcertSeatInfo> seatInfoList = concertService.searchSeat(concert.getId(), command);

		// assert
		// 1) 결과 수
		assertThat(seatInfoList).hasSize(2);

		// 2) 좌석 정보 검증
		//    - section, seatNumber 등이 잘 매핑되었는지, status가 AVAILABLE인지
		assertThat(seatInfoList)
			.extracting("section")
			.containsExactlyInAnyOrder("A", "B");
		assertThat(seatInfoList)
			.extracting("seatNumber")
			.containsExactlyInAnyOrder(1, 1);
		assertThat(seatInfoList)
			.extracting("status")
			.containsOnly(ConcertSeatStatus.AVAILABLE);
	}

	@Test
	void AVAILABLE_상태가_아닌_좌석은_검색결과에_포함되지_않는다() {
		// arrange
		// 1) 콘서트 + 스케줄 저장
		Concert concert = concertJpaRepository.save(
			Concert.builder()
				.concertTitle("테스트 콘서트")
				.artistName("테스트 아티스트")
				.build()
		);
		ConcertSchedule schedule = concertScheduleJpaRepository.save(
			ConcertSchedule.builder()
				.concertDate(LocalDate.of(2025, 6, 30))
				.venue("공연장 테스트")
				.status(ConcertScheduleStatus.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.concert(concert)
				.build()
		);
		// 2) 좌석 중 하나는 AVAILABLE, 하나는 SOLD_OUT
		ConcertSeat seatAvailable = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("C")
				.seatNumber(1)
				.status(ConcertSeatStatus.AVAILABLE)
				.build()
		);
		ConcertSeat seatSoldOut = concertSeatJpaRepository.save(
			ConcertSeat.builder()
				.concertSchedule(schedule)
				.section("C")
				.seatNumber(2)
				.status(ConcertSeatStatus.BOOKED)
				.build()
		);

		ConcertSeatSearchCommand command = new ConcertSeatSearchCommand(
			schedule.getId(),
			schedule.getConcertDate(),
			0,
			10
		);

		// act
		List<ConcertSeatInfo> result = concertService.searchSeat(concert.getId(), command);

		// assert
		// SOLD_OUT 좌석은 조회되지 않아야 함
		assertThat(result).hasSize(1);
		assertThat(result.get(0).section()).isEqualTo("C");
		assertThat(result.get(0).seatNumber()).isEqualTo(1);
		assertThat(result.get(0).status()).isEqualTo(ConcertSeatStatus.AVAILABLE);
	}

}
