package kr.hhplus.be.server.domain.concert;

import java.time.LocalDateTime;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "concerts")
public class Concert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concert_id")
	private Long id;

	@Column(name = "concert_title", length = 100, nullable = false)
	private String concertTitle;

	@Column(name = "artist_name", length = 100, nullable = false)
	private String artistName;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "modified_at")
	private LocalDateTime modifiedAt;

	@Builder
	public Concert(Long id, String concertTitle, String artistName
	) {
		this.id = id;
		this.concertTitle = concertTitle;
		this.artistName = artistName;
		this.createdAt = LocalDateTime.now();
	}
}
