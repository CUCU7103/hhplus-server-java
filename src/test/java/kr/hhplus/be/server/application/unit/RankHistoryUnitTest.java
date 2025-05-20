package kr.hhplus.be.server.application.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.application.rank.RankingHistoryService;
import kr.hhplus.be.server.domain.concert.ConcertRankRepository;
import kr.hhplus.be.server.domain.rank.RankingHistory;
import kr.hhplus.be.server.domain.rank.RankingHistoryRepository;

@ExtendWith(MockitoExtension.class)
public class RankHistoryUnitTest {

	@Mock
	private RankingHistoryRepository rankingHistoryRepository;
	@Mock
	private ConcertRankRepository concertRankRepository;
	@Mock
	private ObjectMapper objectMapper; // Jackson JSON 라이브러리
	@InjectMocks
	private RankingHistoryService rankingHistoryService;

	@Test
	void persistTopRankingsToDB_랭킹없을때_빈리스트반환() {
		// given
		given(concertRankRepository.top5ConcertSchedule()).willReturn(Collections.emptySet());
		// when
		List<RankingHistory> result = rankingHistoryService.persistTopRankingsToDB();

		// then
		assertThat(result).isEmpty();
		verify(rankingHistoryRepository, times(0)).saveAll(anyList());
	}

	/*@Test
	void persistTopRankingsToDB_랭킹존재할때_DB에저장하고_엔티티반환() throws Exception {
		// given
		String json1 = "{\"concertName\":\"A\",\"concertDate\":\"2025-05-10\"}";
		String json2 = "{\"concertName\":\"B\",\"concertDate\":\"2025-05-12\"}";
		Set<String> top5 = Set.of(json1, json2);
		given(concertRankRepository.top5ConcertSchedule()).willReturn(top5);

		// JSON 파싱을 위한 JsonNode 목 생성
		JsonNode node1 = mock(JsonNode.class);
		JsonNode nameNode1 = mock(JsonNode.class);
		JsonNode dateNode1 = mock(JsonNode.class);
		given(objectMapper.readTree(json1)).willReturn(node1);
		given(node1.get("concertName")).willReturn(nameNode1);
		given(nameNode1.asText()).willReturn("A");
		given(node1.get("concertDate")).willReturn(dateNode1);
		given(dateNode1.asText()).willReturn("2025-05-10");

		JsonNode node2 = mock(JsonNode.class);
		JsonNode nameNode2 = mock(JsonNode.class);
		JsonNode dateNode2 = mock(JsonNode.class);
		given(objectMapper.readTree(json2)).willReturn(node2);
		given(node2.get("concertName")).willReturn(nameNode2);
		given(nameNode2.asText()).willReturn("B");
		given(node2.get("concertDate")).willReturn(dateNode2);
		given(dateNode2.asText()).willReturn("2025-05-12");

		LocalDate today = LocalDate.now();

		// when
		List<RankingHistory> result = rankingHistoryService.persistTopRankingsToDB();

		// then
		// 1) 반환된 리스트 크기 및 필드 검증
		assertThat(result).hasSize(2)
			.extracting(RankingHistory::getConcertName, RankingHistory::getConcertDate)
			.containsExactlyInAnyOrder(tuple("A", LocalDate.parse("2025-05-10")),
				tuple("B", LocalDate.parse("2025-05-12")));

		// 2) saveAll 호출 검증
		ArgumentCaptor<List<RankingHistory>> captor = ArgumentCaptor.forClass(List.class);
		verify(rankingHistoryRepository, times(1)).saveAll(captor.capture());

		List<RankingHistory> saved = captor.getValue();
		assertThat(saved).hasSize(2);
	}*/

}
