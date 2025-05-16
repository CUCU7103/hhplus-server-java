package kr.hhplus.be.server.domain.token;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Token {

	private String userId; // 유저아이디
	private Instant issuedAt; // 발급시간
	private double epochSeconds; // Score에 저장하기 위해 double로 변경할 값을 담을 변수
	private long rank;

	public Token(Long userId) {
		this.userId = String.valueOf(userId);
		this.issuedAt = Instant.now();
		this.epochSeconds = issuedAt.toEpochMilli() / 1000.0;
	}

	public Token(long userId, long rank) {
		this.userId = String.valueOf(userId);
		this.rank = rank;
	}

	public static Token create(long userId) {
		return new Token(userId);
	}

	public static Token createIncludeRank(long userId, long rank) {
		return new Token(userId, rank);
	}
}

