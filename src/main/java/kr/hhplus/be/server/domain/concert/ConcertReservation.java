package kr.hhplus.be.server.domain.concert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import kr.hhplus.be.server.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "reservations")
public class ConcertReservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(name = "reservation_status", nullable = false)
	private ConcertReservationStatus concertReservationStatus;

	@Column(name = "created_at", nullable = false, updatable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "modified_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@LastModifiedDate
	private LocalDateTime modifiedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private ConcertSeat concertSeat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_schedule_id", nullable = false)
	private ConcertSchedule concertSchedule;

	@Builder
	public ConcertReservation(long id, BigDecimal price, ConcertReservationStatus concertReservationStatus,
		User user, ConcertSeat concertSeat, ConcertSchedule concertSchedule) {
		this.id = id;
		this.price = price;
		this.concertReservationStatus = concertReservationStatus;
		this.user = user;
		this.concertSeat = concertSeat;
		this.concertSchedule = concertSchedule;
	}

}


