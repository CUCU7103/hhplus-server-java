package kr.hhplus.be.server.domain.balance;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
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
import kr.hhplus.be.server.domain.balance.model.BalanceHistoryInfo;
import kr.hhplus.be.server.domain.balance.model.BalanceType;
import kr.hhplus.be.server.domain.balance.model.PointVO;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "point_histories")
public class BalanceHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "previous_point", nullable = false)
	private BigDecimal previousPoint;

	@Column(name = "delta_point", nullable = false)
	private BigDecimal deltaPoint;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private BalanceType type;

	@Column(name = "created_at", nullable = false)
	@CreatedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "balance_id", nullable = false)
	private Balance balance;

	@Builder
	public BalanceHistory(BigDecimal previousPoint, BigDecimal deltaPoint, BalanceType type, Balance balance) {
		this.previousPoint = previousPoint;
		this.deltaPoint = deltaPoint;
		this.type = type;
		this.balance = balance;
	}

	public static BalanceHistory createdHistory(BalanceHistoryCommand command) {
		return BalanceHistory.builder()
			.previousPoint(command.balance().getPoint())
			.deltaPoint(command.deltaPoint())
			.type(BalanceType.EARN)
			.build();
	}
}
