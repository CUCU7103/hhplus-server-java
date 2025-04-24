package kr.hhplus.be.server.domain.balance.balance;

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
import jakarta.persistence.Version;
import kr.hhplus.be.server.domain.model.MoneyVO;
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
	private MoneyVO moneyVO; // VO로 변경

	@Column(name = "created_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	@CreatedDate
	private LocalDateTime createdAt;

	@Version
	@Column(name = "version")
	private Long version;

	@Column(name = "user_id")
	private long userId;

	@Builder(toBuilder = true)
	public Balance(long id, MoneyVO moneyVO, LocalDateTime createdAt, long userId) {
		this.id = id;
		this.moneyVO = moneyVO;
		this.createdAt = createdAt;
		this.userId = userId;
	}

	@Builder
	public Balance(MoneyVO moneyVO, LocalDateTime createdAt, long userId) {
		this.moneyVO = moneyVO;
		this.createdAt = createdAt;
		this.userId = userId;
		validateField();
	}

	public static Balance create(MoneyVO moneyVO, LocalDateTime createdAt, long userId) {
		return new Balance(moneyVO, createdAt, userId);
	}

	// 포인트 충전 로직
	public Balance chargePoint(BigDecimal chargeAmount) {
		this.moneyVO = this.moneyVO.add(chargeAmount);
		return this;
	}

	// 포인트 사용 로직
	public Balance usePoint(BigDecimal useAmount) {
		this.moneyVO = this.moneyVO.subtract(useAmount);
		return this;
	}

	public void validateField() {
		if (this.moneyVO == null) {
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
