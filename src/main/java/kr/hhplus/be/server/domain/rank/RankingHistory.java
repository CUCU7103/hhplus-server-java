package kr.hhplus.be.server.domain.rank;

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
public class RankingHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer ranking;

	@Column(name = "concert_name", nullable = false)
	private String concertName;

	@Column(name = "concert_date", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private LocalDate concertDate;

	@Column(name = "ranking_date", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private LocalDate rankingDate;

	@Column(name = "created_at")
	@CreatedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;

	@Builder
	public RankingHistory(Integer ranking, String concertName, LocalDate concertDate,
		LocalDate rankingDate) {
		this.ranking = ranking;
		this.concertName = concertName;
		this.concertDate = concertDate;
		this.rankingDate = rankingDate;

	}

	public static RankingHistory create(Integer ranking, String concertName, LocalDate concertDate,
		LocalDate rankingDate) {
		return RankingHistory.builder()
			.ranking(ranking)
			.concertName(concertName)
			.concertDate(concertDate)
			.rankingDate(rankingDate)
			.build();

	}
}
