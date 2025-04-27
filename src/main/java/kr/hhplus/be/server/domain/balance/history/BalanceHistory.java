package kr.hhplus.be.server.domain.balance.history;

import static jakarta.persistence.ConstraintMode.*;

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
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.balance.balance.Balance;
import kr.hhplus.be.server.domain.balance.balance.BalanceType;
import kr.hhplus.be.server.domain.model.MoneyVO;
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

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "amount", column = @Column(name = "previous_point"))
	})
	private MoneyVO previousPoint;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "amount", column = @Column(name = "delta_point"))
	})
	private MoneyVO deltaPoint;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private BalanceType type;

	@Column(name = "created_at", nullable = false)
	@CreatedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "balance_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private Balance balance;

	@Builder
	public BalanceHistory(MoneyVO previousPoint, MoneyVO deltaPoint, BalanceType type, Balance balance) {
		this.previousPoint = previousPoint;
		this.deltaPoint = deltaPoint;
		this.type = type;
		this.balance = balance;
	}

	public static BalanceHistory createdHistory(Balance balance, MoneyVO previousPoint) {
		return BalanceHistory.builder()
			.previousPoint(previousPoint)
			.deltaPoint(balance.getMoneyVO())
			.type(BalanceType.EARN)
			.balance(balance)
			.build();
	}

}
