package kr.hhplus.be.server.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.info.ConcertScheduleInfo;
import kr.hhplus.be.server.domain.concert.info.ConcertSeatInfo;
import kr.hhplus.be.server.domain.concert.model.ConcertScheduleStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import kr.hhplus.be.server.interfaces.concert.request.ConcertDateSearchRequest;
import kr.hhplus.be.server.interfaces.concert.request.ConcertSeatSearchRequest;

@ExtendWith(MockitoExtension.class)
class ConcertSearchUnitTest {

	@Mock
	private ConcertRepository concertRepository;

	@InjectMocks
	private ConcertService concertService;

	@Test
	void 입력받은_콘서트_아이디에_해당하는_콘서트가_없어_예외처리한다() {
		//arrange
		long concertId = 1L;
		String startDate = "2025-06-01";
		String endDate = "2025-06-30";

		// DTO (Request) 객체 준비
		ConcertDateSearchRequest request = new ConcertDateSearchRequest(startDate, endDate);
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
			.concertId(concertId)
			.artistName("윤하")
			.concertTitle("윤하 콘서트")
			.build();

		// DTO (Request) 객체 준비
		ConcertDateSearchRequest request = new ConcertDateSearchRequest(startDate, endDate);

		// stub
		given(concertRepository.findByConcertId(concertId)).willReturn(Optional.of(concert));
		given(concertRepository.getConcertScheduleList(
			concertId,
			request.toCommand().startDate(),
			request.toCommand().endDate(),
			ConcertScheduleStatus.AVAILABLE))
			.willReturn(Collections.emptyList());

		// act
		List<ConcertScheduleInfo> result = concertService.searchDate(concertId, request.toCommand());

		// assert
		// 결과가 빈 리스트인지 확인
		assertThat(result).isEmpty();
	}

	@Test
	void 입력받은_콘서트_스케줄_아이디와_날짜에_해당하는_스케줄_조회에_성공() {
		//arrange
		long concertId = 1L;
		List<Long> concertScheduleIds = Arrays.asList(1L, 2L, 6L);
		String startDate = "2025-06-01";
		String endDate = "2025-06-30";
		LocalDate date1 = LocalDate.of(2025, 6, 2);
		LocalDate date2 = LocalDate.of(2025, 6, 5);
		LocalDate date3 = LocalDate.of(2025, 6, 3);
		Concert concert = mock(Concert.class);
		List<LocalDate> dateTimes = Arrays.asList(date1, date2, date3);
		ConcertDateSearchRequest request = new ConcertDateSearchRequest(startDate, endDate);
		List<ConcertSchedule> schedules = new ArrayList<>();

		for (int i = 0; i < concertScheduleIds.size(); i++) {
			schedules.add(
				ConcertSchedule.of(concertScheduleIds.get(i), "성균관대", dateTimes.get(i), ConcertScheduleStatus.AVAILABLE,
					LocalDateTime.now()));
		}

		given(concertRepository.findByConcertId(concertId)).willReturn(Optional.of(concert));
		given(concertRepository.getConcertScheduleList(concertId,
			request.toCommand().startDate(),
			request.toCommand().endDate(), ConcertScheduleStatus.AVAILABLE)).willReturn(schedules);
		// act
		List<ConcertScheduleInfo> result = concertService.searchDate(concertId, request.toCommand());
		// assert
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(3);
		assertThat(result.get(0).concertDate()).isEqualTo(date1);
		assertThat(result.get(1).id()).isEqualTo(2L);
		assertThat(result.get(1).concertDate()).isEqualTo(date2);
		assertThat(result.get(2).id()).isEqualTo(6L);
		assertThat(result.get(2).concertDate()).isEqualTo(date3);

	}

	@Test
	void 좌석_조회시_입력받은_스케줄_아이디와_날짜에_해당하는_스케줄이_없어_예외처리() {
		//arrange
		long concertId = 1L;
		long concertScheduleId = 1L;
		String concertDate = "2025-06-01";
		// 실제 Concert 엔티티 준비
		Concert concert = Concert.builder()
			.concertId(concertId)
			.artistName("윤하")
			.concertTitle("윤하 콘서트")
			.build();

		// DTO (Request) 객체 준비
		ConcertSeatSearchRequest request = new ConcertSeatSearchRequest(concertScheduleId, concertDate, 0, 10);

		// stub
		given(concertRepository.findByConcertId(concertId)).willReturn(Optional.of(concert));
		// repository가 Optional.empty()를 반환하도록 설정
		given(concertRepository.getConcertSchedule(concertScheduleId,
			request.toCommand().concertDate())).willReturn(Optional.empty());

		// act & assert
		assertThatThrownBy(() -> concertService.searchSeat(concertId, request.toCommand()))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.NOT_FOUND_SCHEDULE.getMessage());
	}

	@Test
	void 좌석_조회시_입력받은_콘서트_스케줄_아이디와_날짜의_예약_가능한_좌석조회에_성공() {
		// arrange - 사전에 Mock 동작 정의 및 테스트에 필요한 데이터 준비
		long concertScheduleId = 1L;
		Concert concert = mock(Concert.class);
		ConcertSchedule concertSchedule = mock(ConcertSchedule.class);
		ConcertSeatSearchRequest request = new ConcertSeatSearchRequest(
			concertScheduleId, "2025-05-20", 0, 10);

		ConcertSeat concertSeat1 = ConcertSeat.builder()
			.id(1L)
			.section("A")
			.seatNumber(10)
			.status(ConcertSeatStatus.AVAILABLE)
			.concertSchedule(concertSchedule)
			.build();

		ConcertSeat concertSeat2 = ConcertSeat.builder()
			.id(1L)
			.section("B")
			.seatNumber(11)
			.status(ConcertSeatStatus.AVAILABLE)
			.concertSchedule(concertSchedule)
			.build();
		List<ConcertSeat> concertSeatList = Arrays.asList(concertSeat1, concertSeat2);

		// Page<Seat> 를 생성하여 페이징된 결과 형태로 Mock 반환 설정
		PageRequest pageable = PageRequest.of(request.page(), request.size(), Sort.by("section"));
		Page<ConcertSeat> seatPage = new PageImpl<>(concertSeatList, pageable, concertSeatList.size());

		// Mock repository 동작 설정
		given(concertRepository.findByConcertId(concertScheduleId)).willReturn(Optional.of(concert));
		given(concertRepository.getConcertSchedule(request.toCommand().concertScheduleId(),
			request.toCommand().concertDate()))
			.willReturn(Optional.of(concertSchedule)); // Concert 엔티티 객체는 실제 코드와 맞게 세팅
		given(concertRepository.findByConcertScheduleIdAndSeatStatusContaining(
			1L, ConcertSeatStatus.AVAILABLE, pageable)
		).willReturn(seatPage);

		// when - 실제 서비스 메서드 호출
		List<ConcertSeatInfo> concertSeatInfos = concertService.searchSeat(concertScheduleId, request.toCommand());

		// then - 결과 검증
		assertThat(concertSeatInfos).isNotNull();
		assertThat(concertSeatInfos).hasSize(2); // seat1, seat2가 들어있으므로
		assertThat(concertSeatInfos.get(0).seatId()).isEqualTo(1L);
		assertThat(concertSeatInfos.get(0).section()).isEqualTo("A");
		assertThat(concertSeatInfos.get(1).seatNumber()).isEqualTo(11L);
		assertThat(concertSeatInfos.get(1).status()).isEqualTo(ConcertSeatStatus.AVAILABLE);
	}

}
