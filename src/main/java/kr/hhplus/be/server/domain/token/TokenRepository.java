package kr.hhplus.be.server.domain.token;

import java.time.Duration;
import java.util.Set;

public interface TokenRepository {

	boolean issueTokenNotExist(Token token);

	long findUserRank(long userId);

	Set<String> top1000WaitingTokens();

	void activeQueue(String userId);

	void pushActiveQueue(String userId, String expireMillis, Duration ttl);

	void removeWaitingTokens(String[] topTokens);

	Set<String> scanActiveQueue();

	boolean hasKey(String userId);

	void removeActiveTokens(String userId);

	boolean existInWaitingQueue(String userId);
}
