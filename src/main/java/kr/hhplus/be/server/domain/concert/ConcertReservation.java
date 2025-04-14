package kr.hhplus.be.server.domain.concert;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import kr.hhplus.be.server.domain.MoneyVO;
import kr.hhplus.be.server.domain.concert.model.ConcertReservationStatus;
import kr.hhplus.be.server.domain.concert.model.ConcertSeatStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
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

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "amount", column = @Column(name = "price"))
	})
	private MoneyVO price;

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

	@Column(name = "expiration_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime expirationAt;

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
	public ConcertReservation(long id, MoneyVO price, ConcertReservationStatus concertReservationStatus,
		User user, ConcertSeat concertSeat, ConcertSchedule concertSchedule) {
		this.id = id;
		this.price = price;
		this.concertReservationStatus = concertReservationStatus;
		this.user = user;
		this.concertSeat = concertSeat;
		this.concertSchedule = concertSchedule;
		this.createdAt = LocalDateTime.now();
		this.expirationAt = LocalDateTime.now().plusMinutes(5);
	}

	public static ConcertReservation createPendingReservation(User user, ConcertSeat seat, ConcertSchedule schedule,
		ConcertReservationStatus status) {
		// 예약 전 검증 진행
		validateBeforeCreatedReservation(seat, status);

		seat.changeStatus(ConcertSeatStatus.HELD);

		return ConcertReservation.builder()
			.price(seat.getPrice())
			.concertSeat(seat)
			.concertSchedule(schedule)
			.user(user)
			.concertReservationStatus(status)
			.build();
	}

	public static void validateBeforeCreatedReservation(ConcertSeat seat, ConcertReservationStatus status) {
		// 좌석 상태 검증
		if (seat.getStatus() != ConcertSeatStatus.AVAILABLE) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
		if (status != ConcertReservationStatus.HELD) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
	}

	public void validateStatus() {
		if (this.concertReservationStatus == ConcertReservationStatus.HELD) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
	}

	public void confirm() {
		isReservationConfirmed();
		// 관련 객체들의 상태도 함께 변경
		this.concertSeat.changeStatus(ConcertSeatStatus.BOOKED);
		this.concertReservationStatus = ConcertReservationStatus.BOOKED;

	}

	public void isReservationConfirmed() {
		if (this.concertReservationStatus != ConcertReservationStatus.HELD) {
			throw new CustomException(CustomErrorCode.NOT_HELD_RESERVATION);
		}
	}

	// 제한시간 초과로 인한 취소 메서드
	public void cancelDueToTimeout(LocalDateTime dateTime) {
		isReservationConfirmed();
		if (this.expirationAt.isBefore(dateTime)) {
			// HELD -> avaliable
			concertSeat.changeStatus(ConcertSeatStatus.AVAILABLE);
			this.concertReservationStatus = ConcertReservationStatus.AVAILABLE;
		}
	}
}


