package kr.hhplus.be.server.domain.balance.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointVO {
	private static final BigDecimal MAX_POINT = BigDecimal.valueOf(100_000L);

	@Column(name = "point") // 엔티티에 매핑될 DB 컬럼명
	private BigDecimal amount;

	private PointVO(BigDecimal amount) {
		validate(amount);
		this.amount = amount;
	}

	public static PointVO of(BigDecimal amount) {
		return new PointVO(amount);
	}

	private void validate(BigDecimal value) {
		// 필요한 검증 로직을 여기서 처리
		if (value.compareTo(BigDecimal.ZERO) < 0) {
			throw new CustomException(CustomErrorCode.OVER_USED_POINT);
		}
		if (value.compareTo(MAX_POINT) > 0) {
			throw new CustomException(CustomErrorCode.OVER_CHARGED_POINT);
		}
	}

	public PointVO add(BigDecimal other) {
		BigDecimal newValue = this.amount.add(other);
		return new PointVO(newValue);
	}

	public PointVO subtract(BigDecimal other) {
		BigDecimal newValue = this.amount.subtract(other);
		return new PointVO(newValue);
	}

}
