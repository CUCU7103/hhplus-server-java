package kr.hhplus.be.server.domain.token;

import java.time.LocalDateTime;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tokens")
@Getter
public class Token {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TokenStatus status;

	@Column(name = "token_value", nullable = false, length = 255)
	private String tokenValue;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "modified_at", nullable = false)
	private LocalDateTime modifiedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public static Token createToken(User user, TokenStatus status, String tokenValue) {
		return new Token(user, status, tokenValue);
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

}
