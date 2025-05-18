package kr.hhplus.be.server.domain.concert.schedule;

import static jakarta.persistence.ConstraintMode.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "concert_schedules")
public class ConcertSchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "venue", nullable = false)
	private String venue;

	@Column(name = "concert_date", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private LocalDate concertDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "concert_open_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime concertOpenDate;

	@Column(name = "concert_schedule_status")
	@Enumerated(EnumType.STRING)
	private ConcertScheduleStatus status;

	// concert_id와의 관계 설정
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private Concert concert;

	@Builder
	public ConcertSchedule(String venue, LocalDate concertDate, ConcertScheduleStatus status,
		LocalDateTime createdAt, Concert concert) {
		this.venue = venue;
		this.concertDate = concertDate;
		this.status = status;
		this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
		this.concert = concert;
		validateField();
	}

	public static ConcertSchedule of(String venue, LocalDate concertDate, ConcertScheduleStatus status,
		LocalDateTime createdAt, Concert concert) {
		return ConcertSchedule.builder()
			.venue(venue)
			.concertDate(concertDate)
			.createdAt(createdAt)
			.status(status)
			.concert(concert)
			.build();
	}

	private void validateField() {
		if (this.venue.isEmpty() || this.concertDate == null || this.status == null || this.concert == null) {
			throw new CustomException(CustomErrorCode.EMPTY_FIELD);
		}
	}

}
