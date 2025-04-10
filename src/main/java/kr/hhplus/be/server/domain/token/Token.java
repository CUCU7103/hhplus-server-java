package kr.hhplus.be.server.domain.token;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
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
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tokens")
@Getter
public class Token {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TokenStatus status;

	@Column(name = "token_value", nullable = false, length = 255)
	private String tokenValue;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "modified_at", nullable = false)
	private LocalDateTime modifiedAt;

	@Column(name = "expiration_at")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime expirationAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder
	public Token(User user, LocalDateTime modifiedAt, LocalDateTime createdAt, String tokenValue, TokenStatus status,
		long id) {
		this.user = user;
		this.modifiedAt = modifiedAt;
		this.createdAt = createdAt;
		this.tokenValue = tokenValue;
		this.status = status;
		this.id = id;
	}

	public static Token createToken(User user, TokenStatus status, String tokenValue) {
		return Token.builder()
			.user(user)
			.status(status)
			.tokenValue(tokenValue)
			.build();
	}

	private Token(User user, TokenStatus status, String tokenValue) {
		this.user = user;
		this.status = status;
		this.tokenValue = tokenValue;
	}

	public Token updateStatus(TokenStatus status) {
		this.status = status;
		return this;
	}

	/**
	 * 토큰이 WAITING 상태에서 외부에서 전달받은 waitingRank, activeTokenCount, maxActive 정보를 활용하여
	 * 조건을 만족할 경우 ACTIVE 상태로 전환하는 메서드
	 *
	 * @param waitingRank      대기 순위
	 * @param activeTokenCount 현재 ACTIVE 상태 토큰 수
	 * @param maxActive        최대 허용 ACTIVE 토큰 수
	 */

	final long MAX_ACTIVE = 1000;

	public void checkAndActivate(int waitingRank, long activeTokenCount) {
		if (!TokenStatus.WAITING.equals(this.getStatus())) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
		// 대기순위가 1이고, 현재 활성화 토큰 수가 maxActive 미만이면 ACTIVE 상태로 전환
		if (waitingRank == 1 && activeTokenCount < MAX_ACTIVE) {
			updateStatus(TokenStatus.ACTIVE);
		}
		// 그렇지 않으면 상태를 변경하지 않습니다.
	}

}
