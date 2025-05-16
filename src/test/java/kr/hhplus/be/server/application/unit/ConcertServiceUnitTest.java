package kr.hhplus.be.server.application.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.command.ConcertDateSearchCommand;
import kr.hhplus.be.server.application.concert.info.ConcertScheduleInfo;
import kr.hhplus.be.server.application.concert.info.ConcertSeatInfo;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleCashRepository;
import kr.hhplus.be.server.domain.concert.schedule.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.presentation.concert.request.ConcertDateSearchRequest;
import kr.hhplus.be.server.presentation.concert.request.ConcertSeatSearchRequest;

@ExtendWith(MockitoExtension.class)
class ConcertServiceUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private ConcertScheduleCashRepository concertScheduleCashRepository;

	@InjectMocks
	private ConcertService concertService;

	@Test
	void 입력받은_콘서트_아이디에_해당하는_콘서트가_없어_예외처리한다() {
		//arrange
		long concertId = 1L;
		String startDate = "2025-06-01";
		String endDate = "2025-06-30";

		// DTO (Request) 객체 준비
		ConcertDateSearchRequest request = new ConcertDateSearchRequest(startDate, endDate, 1, 20);
		// repository가 Optional.empty()를 반환하도록 설정
		given(concertRepository.findByConcertId(concertId)).willReturn(Optional.empty());

		// act & assert
		assertThatThrownBy(() -> concertService.searchDate(concertId, request.toCommand()))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_CONCERT.getMessage());
	}

	@Test
	void 날짜_조회시_스케줄이_없으면_빈_리스트를_반환한다() {
		// arrange
		long concertId = 1L;
		String startDate = "2025-06-01";
		String endDate = "2025-06-30";

		// 실제 Concert 엔티티 준비
		Concert concert = Concert.builder()
			.id(concertId)
			.artistName("윤하")
			.concertTitle("윤하 콘서트")
			.build();

		// DTO (Request) 객체 준비
		ConcertDateSearchRequest request = new ConcertDateSearchRequest(startDate, endDate, 1, 20);

		// stub
		given(concertRepository.findByConcertId(concertId)).willReturn(Optional.of(concert));
		given(concertRepository.getConcertScheduleListOrderByDate(
			concertId,
			request.toCommand().startDate(),
			request.toCommand().endDate(),
			ConcertScheduleStatus.AVAILABLE,
			Sort.by("concertDate").descending()))
			.willReturn(Collections.emptyList());

		// act
		List<ConcertScheduleInfo> result = concertService.searchDate(concertId, request.toCommand());

		// assert
		// 결과가 빈 리스트인지 확인
		assertThat(result).isEmpty();
	}

	@Test
	void 입력받은_콘서트_스케줄_아이디와_날짜에_해당하는_스케줄_조회에_성공() {
		// arrange
		long concertId = 1L;
		List<Long> concertScheduleIds = List.of(1L, 2L, 6L);
		List<LocalDate> dateTimes = List.of(
			LocalDate.of(2025, 6, 2),
			LocalDate.of(2025, 6, 5),
			LocalDate.of(2025, 6, 3)
		);
		Concert concert = Concert.builder()
			.id(concertId)
			.artistName("윤하")
			.concertTitle("윤하 콘서트")
			.build();

		List<ConcertSchedule> schedules = new ArrayList<>();
		for (int i = 0; i < concertScheduleIds.size(); i++) {
			ConcertSchedule schedule = ConcertSchedule.of(
				"성균관대", dateTimes.get(i), ConcertScheduleStatus.AVAILABLE,
				LocalDateTime.now(), concert
			);
			// 방법2) setId()가 없으면 (Reflection 사용)
			ReflectionTestUtils.setField(schedule, "id", concertScheduleIds.get(i));

			schedules.add(schedule);
		}

		given(concertRepository.findByConcertId(concertId))
			.willReturn(Optional.of(concert));
		given(concertRepository.getConcertScheduleListOrderByDate(
			concertId,
			LocalDate.of(2025, 6, 1),
			LocalDate.of(2025, 6, 30),
			ConcertScheduleStatus.AVAILABLE,
			Sort.by("concertDate").descending()
		))
			.willReturn(schedules);

		// act
		List<ConcertScheduleInfo> result = concertService.searchDate(
			concertId,
			new ConcertDateSearchCommand(
				LocalDate.of(2025, 6, 1),
				LocalDate.of(2025, 6, 30),
				1, 20
			)
		);

		// assert
		assertThat(result).hasSize(3);
		assertThat(result.get(0).id()).isEqualTo(1L);
		assertThat(result.get(1).id()).isEqualTo(2L);
		assertThat(result.get(2).id()).isEqualTo(6L);
	}

	@Test
	void 좌석_조회시_입력받은_스케줄_아이디와_날짜에_해당하는_스케줄이_없어_예외처리() {
		//arrange
		long concertId = 1L;
		long concertScheduleId = 1L;
		String concertDate = "2025-06-01";
		// 실제 Concert 엔티티 준비
		Concert concert = Concert.builder()
			.id(concertId)
			.artistName("윤하")
			.concertTitle("윤하 콘서트")
			.build();

		// DTO (Request) 객체 준비
		ConcertSeatSearchRequest request = new ConcertSeatSearchRequest(concertDate, 0, 10);

		// stub
		// repository가 Optional.empty()를 반환하도록 설정
		given(concertRepository.getConcertScheduleWithDate(concertScheduleId,
			request.toCommand().concertDate())).willReturn(Optional.empty());

		// act & assert
		assertThatThrownBy(() -> concertService.searchSeat(concertId, request.toCommand()))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_SCHEDULE.getMessage());
	}

	@Test
	void 좌석_조회시_입력받은_콘서트_스케줄_아이디와_날짜의_예약_가능한_좌석조회에_성공() {
		// arrange
		long concertScheduleId = 1L;
		long concertId = 1L;              // ← Concert ID 도 명시
		Concert concert = mock(Concert.class);
		ConcertSchedule schedule = mock(ConcertSchedule.class);

		// mock Concert#getId() → 1L
		when(concert.getId()).thenReturn(concertId);
		// mock schedule.getConcert() → concert
		when(schedule.getConcert()).thenReturn(concert);

		ConcertSeatSearchRequest request =
			new ConcertSeatSearchRequest("2025-05-20", 0, 10);

		// 예시 좌석 엔티티
		ConcertSeat seat1 = ConcertSeat.builder()
			.id(1L).section("A").seatNumber(10)
			.status(ConcertSeatStatus.AVAILABLE)
			.concertSchedule(schedule).build();
		ConcertSeat seat2 = ConcertSeat.builder()
			.id(2L).section("B").seatNumber(11)
			.status(ConcertSeatStatus.AVAILABLE)
			.concertSchedule(schedule).build();
		List<ConcertSeat> seatList = List.of(seat1, seat2);

		PageRequest pageable = PageRequest.of(request.page(), request.size(), Sort.by("section"));
		Page<ConcertSeat> seatPage = new PageImpl<>(seatList, pageable, seatList.size());

		// stub repository
		given(concertRepository.getConcertScheduleWithDate(
			concertScheduleId,
			request.toCommand().concertDate()))
			.willReturn(Optional.of(schedule));

		given(concertRepository.findByConcertId(concertId))
			.willReturn(Optional.of(concert));

		given(concertRepository.findByConcertScheduleIdAndSeatStatusContaining(
			concertScheduleId, ConcertSeatStatus.AVAILABLE, pageable))
			.willReturn(seatPage);

		// when
		List<ConcertSeatInfo> infos =
			concertService.searchSeat(concertScheduleId, request.toCommand());

		// then
		assertThat(infos).hasSize(2);
		assertThat(infos.get(0).section()).isEqualTo("A");
		assertThat(infos.get(1).seatNumber()).isEqualTo(11);
	}

}
