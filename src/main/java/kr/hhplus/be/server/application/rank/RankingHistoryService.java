package kr.hhplus.be.server.application.rank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistory;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingHistoryRepository;
import kr.hhplus.be.server.domain.concert.rank.ConcertRankingRepository;
import kr.hhplus.be.server.domain.payment.event.RankContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingHistoryService {

	private final ConcertRankingHistoryRepository concertRankingHistoryRepository;
	private final ConcertRankingRepository concertRankingRepository;
	private final ObjectMapper objectMapper; // Jackson JSON 라이브러리

	/**
	 * Redis의 상위 5개 랭킹 데이터를 DB에 저장합니다.
	 *
	 * @return 저장된 레코드 수
	 */
	@Transactional
	public List<ConcertRankingHistory> persistTopRankingsToDB() {
		LocalDate today = LocalDate.now();

		// 기존 top5ConcertSchedule() 메서드 활용
		Set<RankContext> top5Concerts = concertRankingRepository.top5ConcertSchedule();

		if (top5Concerts == null || top5Concerts.isEmpty()) {
			log.info("저장할 랭킹이 존재하지 않습니다");
			// 빈 리스트 반환
			return new ArrayList<>();
		}

		// Redis에서 각 콘서트의 selloutTime 가져오기
		List<ConcertRankingHistory> rankingEntities = new ArrayList<>();
		int rank = 1;

		for (RankContext concertJson : top5Concerts) {
			try {
				// JSON 파싱
				JsonNode jsonNode = objectMapper.readTree(String.valueOf(concertJson));

				String concertName = jsonNode.get("concertName").asText();
				LocalDate concertDate = LocalDate.parse(jsonNode.get("concertDate").asText());
				// 엔티티 생성 및 추가
				rankingEntities.add(ConcertRankingHistory.create(rank, concertName, concertDate, today)
				);

			} catch (Exception e) {
				log.error("JSON 파싱 오류: {}", e.getMessage(), e);
				// 오류 시 해당 항목 건너뛰기
			}
		}
		// DB에 저장
		concertRankingHistoryRepository.saveAll(rankingEntities);
		return rankingEntities;
	}

}
