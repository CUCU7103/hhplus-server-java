package kr.hhplus.be.server.domain.concert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
@Getter
public class ConcertPayment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@Column(name = "created_at", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "modified_at", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@LastModifiedDate
	private LocalDateTime modifiedAt;

	@ManyToOne
	@JoinColumn(name = "reservation_id", nullable = false)
	private ConcertReservation reservation;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 정적 팩토리 메서드 - 새로운 결제 생성
	public static ConcertPayment createPayment(ConcertReservation reservation, User user, BigDecimal price) {
		return new ConcertPayment(reservation, user, price);
	}

	private ConcertPayment(ConcertReservation reservation, User user, BigDecimal price) {
		validatePaymentData(reservation, user, price);
		this.reservation = reservation;
		this.user = user;
		this.price = price;
	}

	// 유효성 검증 메서드
	private void validatePaymentData(ConcertReservation reservation, User user, BigDecimal price) {
		if (reservation == null) {
			throw new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION);
		}
		if (user == null) {
			throw new CustomException(CustomErrorCode.NOT_FOUND_USER);
		}
		if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_PAYMENT_AMOUNT);
		}
	}

}
