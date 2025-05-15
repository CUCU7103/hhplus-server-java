package kr.hhplus.be.server.domain.token;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public interface TokenRepository {

	boolean issueTokenNotExist(Token token);

	Optional<Token> findByUserId(long userId);

	Token save(Token token);
	
	// redis 전용 메서드 추가
	long findUserRank(long userId);

	Set<String> top1000WaitingTokens();

	void activeQueue(String userId);

	void pushActiveQueue(String userId, String expireMillis, Duration ttl);

	void removeWaitingTokens(Object topTokens);

	Set<String> scanActiveQueue();

	boolean hasKey(String userId);

	void removeActiveTokens(String userId);
}
