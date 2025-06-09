package kr.hhplus.be.server.domain.concert.rank;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ranking_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertRankingHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ranking")
	private Integer ranking;

	@Column(name = "concert_name")
	private String concertName;

	@Column(name = "concert_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private LocalDate concertDate;

	@Column(name = "ranking_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private LocalDate rankingDate;

	@Column(name = "score")
	private long score;

	@Column(name = "created_at")
	@CreatedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;

	protected static final String RANK_KEY = "concert:selloutTime";

	@Builder
	public ConcertRankingHistory(Integer ranking, String concertName, LocalDate concertDate,
		LocalDate rankingDate, long score) {
		this.ranking = ranking;
		this.concertName = concertName;
		this.concertDate = concertDate;
		this.rankingDate = rankingDate;
		this.score = score;

	}

	public static ConcertRankingHistory create(Integer ranking, String concertName, LocalDate concertDate,
		LocalDate rankingDate) {
		return ConcertRankingHistory.builder()
			.ranking(ranking)
			.concertName(concertName)
			.concertDate(concertDate)
			.rankingDate(rankingDate)
			.build();

	}

	public static ConcertRankingHistory createBackup(String concertName, LocalDate concertDate, long score) {
		return ConcertRankingHistory.builder()
			.concertName(concertName)
			.concertDate(concertDate)
			.score(score)
			.build();
	}

	public static String getRankKey() {
		return RANK_KEY;
	}
}
