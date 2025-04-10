package kr.hhplus.be.server.domain.concert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "seats")
public class ConcertSeat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "section", nullable = false)
	private String section;

	@Column(name = "seat_number", nullable = false)
	private Integer seatNumber;

	// "AVAILABLE", "HELD", "BOOKED" 등과 같은 문자열을 저장할 수 있음
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ConcertSeatStatus status;

	@Column(name = "price")
	private BigDecimal price;

	@Column(name = "created_at", nullable = false, updatable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "modified_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@LastModifiedDate
	private LocalDateTime modifiedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_schedule_id", nullable = false)
	private ConcertSchedule concertSchedule;

	@Builder
	public ConcertSeat(ConcertSchedule concertSchedule, LocalDateTime createdAt, ConcertSeatStatus status,
		Integer seatNumber, String section, Long id) {
		this.id = id;
		this.section = section;
		this.seatNumber = seatNumber;
		this.status = status;
		this.concertSchedule = concertSchedule;
	}

	public ConcertSeat changeStatus(ConcertSeatStatus newStatus) {
		this.status = newStatus;
		return this;
	}

}
