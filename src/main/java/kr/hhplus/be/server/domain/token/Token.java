package kr.hhplus.be.server.domain.token;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Token {

	private String userId; // 유저아이디
	private Instant issuedAt; // 발급시간 , Instant
	private double epochSeconds; // Score에 저장하기 위해 double로 변경할 값을 담을 변수
	private long rank;

	protected static final String WAITING_KEY = "waiting:tokens";
	protected static final String ACTIVE_KEY = "active:tokens";

	public Token(Long userId) {
		this.userId = String.valueOf(userId);
		this.issuedAt = Instant.now();
		this.epochSeconds =
			issuedAt.toEpochMilli() / 1000.0; // Instant.toEpochMilli()는 1970-01-01 00:00:00 UTC부터의 경과 밀리초를 long으로 반환
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

	public static String getWaitingQueueKey() {
		return WAITING_KEY;
	}

	public static String getActiveQueueKey() {
		return ACTIVE_KEY;
	}

	public static String getActiveQueueSpecificKey(String userId) {
		return ACTIVE_KEY + ":" + userId;
	}

}

