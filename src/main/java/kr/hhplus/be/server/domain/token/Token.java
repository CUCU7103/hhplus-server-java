package kr.hhplus.be.server.domain.token;

import static jakarta.persistence.ConstraintMode.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
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
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
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
		this.expirationAt = createdAt.plusMinutes(10);
	}

	public static Token createToken(User user) {
		return Token.builder()
			.user(user)
			.status(TokenStatus.WAITING)
			.tokenValue(UUID.randomUUID().toString())
			.createdAt(LocalDateTime.now())
			.build();
	}

	// 토큰 만료기능
	public void expiredToken() {
		if (this.status == TokenStatus.EXPIRED) {
			throw new CustomException(CustomErrorCode.TOKEN_EXPIRED);
		}
		this.status = TokenStatus.EXPIRED;
	}

	/**
	 * 토큰이 WAITING 상태에서 외부에서 전달받은 waitingRank, activeTokenCount
	 * 조건을 만족할 경우 ACTIVE 상태로 전환하는 메서드
	 */

	private static final long MAX_ACTIVE = 1000;

	public void activateToken(int waitingRank, long activeTokenCount) {
		if (!TokenStatus.WAITING.equals(this.getStatus())) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
		// 대기순위가 1이고, 현재 활성화 토큰 수가 maxActive 미만이면 ACTIVE 상태로 전환
		if (waitingRank == 1 && activeTokenCount < MAX_ACTIVE) {
			this.status = TokenStatus.ACTIVE;
		}
		// 그렇지 않으면 상태를 변경하지 않습니다.
	}

	public void expireTokenIfTimedOut() {
		// 토큰 상태가 ACTIVE이면서, 생성시간 기준 10분이 지난 경우 만료 처리
		if (TokenStatus.ACTIVE.equals(this.status) &&
			this.expirationAt.isAfter(createdAt.plusMinutes(10))) {
			expiredToken();
		}
	}

	public void validateTokenStatus() {
		if (TokenStatus.EXPIRED.equals(this.status)) {
			throw new CustomException(CustomErrorCode.TOKEN_EXPIRED);
		}
		if (TokenStatus.WAITING.equals(this.status)) {
			throw new CustomException(CustomErrorCode.INVALID_STATUS);
		}
	}
}

