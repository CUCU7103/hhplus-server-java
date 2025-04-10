package kr.hhplus.be.server.domain.balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(EntityListeners.class)
@Getter
public class Balance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "point")
	private BigDecimal point;

	@Column(name = "created_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private User user;

	@Builder(toBuilder = true)
	public Balance(long id, BigDecimal point, LocalDateTime createdAt, User user) {
		this.id = id;
		this.point = point;
		this.createdAt = createdAt;
		this.user = user;
	}

	@Builder
	public Balance(BigDecimal point, LocalDateTime createdAt, User user) {
		this.point = point;
		this.createdAt = createdAt;
		this.user = user;
	}

	public static Balance of(BigDecimal point, LocalDateTime createdAt, User user) {
		return new Balance(point, createdAt, user);
	}

	final BigDecimal MAX_POINT = BigDecimal.valueOf(100_000L);

	public Balance chargePoint(BigDecimal chargePoint) {
		BigDecimal newPoint = this.point.add(chargePoint);
		chargeValidatePoint(newPoint);
		this.point = newPoint;
		return this;
	}

	public void chargeValidatePoint(BigDecimal point) {
		if (point.compareTo(MAX_POINT) > 0) {
			throw new CustomException(CustomErrorCode.OVER_CHARGED_POINT);
		}
	}

	public Balance usePoint(BigDecimal usePoint) {
		BigDecimal newPoint = this.point.subtract(usePoint);
		useValidatePoint(newPoint);
		this.point = newPoint;
		return this;
	}

	public void useValidatePoint(BigDecimal usePoint) {
		if (usePoint.compareTo(BigDecimal.ZERO) < 0) {
			throw new CustomException(CustomErrorCode.OVER_USED_POINT);
		}
	}
}
