package kr.hhplus.be.server.domain.balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.balance.model.PointVO;
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

	@Embedded
	private PointVO pointVO; // VO로 변경

	@Column(name = "created_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	/**
	 *  반드시 잔액이 User 객체를 들고 있어야 하는가?
	 *  굳이 그럴 필요가 없다고 보여짐
	 * */
	@Column(name = "user_id")
	private long userId;

	@Builder(toBuilder = true)
	public Balance(long id, PointVO pointVO, LocalDateTime createdAt, long userId) {
		this.pointVO = pointVO;
		this.createdAt = createdAt;
		this.userId = userId;
	}

	@Builder
	public Balance(PointVO pointVO, LocalDateTime createdAt, long userId) {
		this.pointVO = pointVO;
		this.createdAt = createdAt;
		this.userId = userId;
		validateField();
	}

	public static Balance of(PointVO pointVO, LocalDateTime createdAt, long userId) {
		return new Balance(pointVO, createdAt, userId);
	}

	// 포인트 충전 로직
	public Balance chargePoint(BigDecimal chargeAmount) {
		this.pointVO = this.pointVO.add(chargeAmount);
		return this;
	}

	// 포인트 사용 로직
	public Balance usePoint(BigDecimal useAmount) {
		this.pointVO = this.pointVO.subtract(useAmount);
		return this;
	}

	public void validateField() {
		if (this.pointVO == null) {
			throw new CustomException(CustomErrorCode.INVALID_POINT);
		}
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
		if (this.userId == 0) {
			throw new CustomException(CustomErrorCode.INVALID_USER_ID);
		}
	}

}
