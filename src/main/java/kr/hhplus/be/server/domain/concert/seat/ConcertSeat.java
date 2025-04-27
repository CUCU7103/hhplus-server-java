package kr.hhplus.be.server.domain.concert.seat;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import jakarta.persistence.Version;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.model.MoneyVO;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
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

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "amount", column = @Column(name = "price"))
	})
	private MoneyVO price;

	@Column(name = "created_at", nullable = false, updatable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@Version
	@Column(name = "version")
	private Long version;

	@Column(name = "modified_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@LastModifiedDate
	private LocalDateTime modifiedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_schedule_id", nullable = false)
	private ConcertSchedule concertSchedule;

	@Builder
	public ConcertSeat(ConcertSchedule concertSchedule, ConcertSeatStatus status,
		Integer seatNumber, String section, Long id, MoneyVO price) {
		this.id = id;
		this.section = section;
		this.seatNumber = seatNumber;
		this.status = status;
		this.concertSchedule = concertSchedule;
		this.price = price;
		this.createdAt = LocalDateTime.now();
	}

	public static ConcertSeat of(ConcertSchedule concertSchedule, ConcertSeatStatus status,
		Integer seatNumber, String section, Long id, MoneyVO price) {
		return ConcertSeat.builder()
			.concertSchedule(concertSchedule)
			.status(status)
			.seatNumber(seatNumber)
			.section(section)
			.id(id)
			.price(price)
			.build();
	}

	public void changeStatus(ConcertSeatStatus newStatus) {
		this.status = newStatus;
	}

	public void validateStatus() {
		if (this.status != ConcertSeatStatus.AVAILABLE) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
	}

}
