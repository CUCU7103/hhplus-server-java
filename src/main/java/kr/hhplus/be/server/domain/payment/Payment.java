package kr.hhplus.be.server.domain.payment;

import static jakarta.persistence.ConstraintMode.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.token.Token;
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
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "created_at", nullable = false, updatable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "modified_at", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@LastModifiedDate
	private LocalDateTime modifiedAt;

	@Column(name = "reservation_id")
	private long reservationId;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private User user;

	// 정적 팩토리 메서드 - 새로운 결제 생성
	public static Payment createPayment(Reservation reservation, User user, BigDecimal price, ConcertSeat concertSeat,
		Balance balance, Token token) {
		return new Payment(reservation, user, price, concertSeat, balance, token);
	}

	// reservationId 말고 reservation을 사용하는 이유??
	// reservationId 는 long이다 도메인 객체의 기능에서 이 값이 진짜 reservation에 존재하는 아이디인지 알 수있을까?
	// 객체를 받아와서 값을 사용하면 명확하게 해결이 가능하다.
	private Payment(Reservation reservation, User user, BigDecimal amount, ConcertSeat concertSeat, Balance balance,
		Token token) {
		validatePaymentData(reservation.getId(), user, amount, concertSeat, balance, token);
		balance.usePoint(amount);
		reservation.confirm();
		token.expiredToken();
		this.reservationId = reservation.getId();
		this.user = user;
		this.amount = amount;
		this.createdAt = LocalDateTime.now();
	}

	// 유효성 검증 메서드
	public void validatePaymentData(long reservationId, User user, BigDecimal price, ConcertSeat concertSeat,
		Balance balance, Token token) {
		if (reservationId <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_RESERVATION_ID);
		}
		if (user == null) {
			throw new CustomException(CustomErrorCode.NOT_FOUND_USER);
		}
		if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_PAYMENT_AMOUNT);
		}
		if (concertSeat == null) {
			throw new CustomException(CustomErrorCode.NOT_FOUND_CONCERT_SEAT);
		}
		if (balance == null) {
			throw new CustomException(CustomErrorCode.NOT_FOUND_BALANCE);
		}
		if (token == null) {
			throw new CustomException(CustomErrorCode.NOT_FOUND_TOKEN);
		}
	}

}
