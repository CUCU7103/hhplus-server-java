package kr.hhplus.be.server.domain.reservation;

import static jakarta.persistence.ConstraintMode.*;

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
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.model.MoneyVO;
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
public class Reservation {

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
	private ReservationStatus reservationStatus;

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
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private ConcertSeat concertSeat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_schedule_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private ConcertSchedule concertSchedule;

	@Builder
	public Reservation(MoneyVO price, ReservationStatus reservationStatus,
		User user, ConcertSeat concertSeat, ConcertSchedule concertSchedule) {
		this.price = price;
		this.reservationStatus = reservationStatus;
		this.user = user;
		this.concertSeat = concertSeat;
		this.concertSchedule = concertSchedule;
		this.createdAt = LocalDateTime.now();
		this.expirationAt = LocalDateTime.now().plusMinutes(5);
	}
	
	public static Reservation createPendingReservation(User user, ConcertSeat seat, ConcertSchedule schedule,
		ReservationStatus status) {
		// 예약 전 검증 진행
		validateBeforeCreatedReservation(seat, status);
		seat.changeStatus(ConcertSeatStatus.HELD);

		return Reservation.builder()
			.price(seat.getPrice())
			.concertSeat(seat)
			.concertSchedule(schedule)
			.user(user)
			.reservationStatus(status)
			.build();
	}

	public static void validateBeforeCreatedReservation(ConcertSeat seat, ReservationStatus status) {
		// 좌석 상태 검증
		if (seat.getStatus() != ConcertSeatStatus.AVAILABLE) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
		if (status != ReservationStatus.HELD) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
	}

	// 결제완료 될 시 예약확정으로 변경
	public void confirm() {
		if (this.reservationStatus != ReservationStatus.HELD) {
			throw new CustomException(CustomErrorCode.NOT_HELD_RESERVATION);
		}
		concertSeat.validateStatus();
		// 관련 객체들의 상태도 함께 변경
		this.concertSeat.changeStatus(ConcertSeatStatus.BOOKED);
		this.reservationStatus = ReservationStatus.BOOKED;

	}

	// 제한시간 초과로 인한 취소 메서드
	public void cancelDueToTimeout(LocalDateTime dateTime) {
		if (this.reservationStatus != ReservationStatus.HELD) {
			throw new CustomException(CustomErrorCode.NOT_HELD_RESERVATION);
		}
		if (this.expirationAt.isBefore(dateTime)) {
			// HELD -> avaliable
			concertSeat.changeStatus(ConcertSeatStatus.AVAILABLE);
			this.reservationStatus = ReservationStatus.AVAILABLE;
		}
	}
}


