package kr.hhplus.be.server.domain.concert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "concerts")
public class Concert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concert_id")
	private Long concertId;

	@Column(name = "concert_title", length = 100, nullable = false)
	private String concertTitle;

	@Column(name = "artist_name", length = 100, nullable = false)
	private String artistName;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "modified_at")
	private LocalDateTime modifiedAt;

	// 콘서트와 스케줄은 1:N 관계
	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConcertSchedule> concertSchedules = new ArrayList<>();

	@Builder
	public Concert(Long concertId, String concertTitle, String artistName
	) {
		this.concertId = concertId;
		this.concertTitle = concertTitle;
		this.artistName = artistName;
	}
}
