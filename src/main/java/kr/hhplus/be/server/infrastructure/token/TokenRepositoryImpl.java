package kr.hhplus.be.server.infrastructure.token;

import java.time.Duration;
import java.util.Set;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {
	private final TokenRedisRepository tokenRedisRepository;

	@Override
	public boolean issueTokenNotExist(Token token) {
		return tokenRedisRepository.issueTokenNotExist(token);
	}

	@Override
	public long findUserRank(long userId) {
		return tokenRedisRepository.findUserRank(userId);
	}

	@Override
	public Set<String> top1000WaitingTokens() {
		return tokenRedisRepository.top1000WaitingTokens();
	}

	@Override
	public void activeQueue(String userId) {
		tokenRedisRepository.activeQueue(userId);
	}

	@Override
	public void pushActiveQueue(String userId, String expireMillis, Duration ttl) {
		tokenRedisRepository.pushActiveQueue(userId, expireMillis, ttl);
	}

	@Override
	public void removeWaitingTokens(String[] topTokens) {
		tokenRedisRepository.removeWaitingTokens(topTokens);
	}

	@Override
	public Set<String> scanActiveQueue() {
		return tokenRedisRepository.scanActiveQueue();
	}

	@Override
	public boolean hasKey(String userId) {
		return tokenRedisRepository.hasKey(userId);
	}

	@Override
	public void removeActiveTokens(String userId) {
		tokenRedisRepository.removeActiveTokens(userId);
	}

	@Override
	public boolean existInWaitingQueue(String userId) {
		return tokenRedisRepository.existsInSortedSet(userId);
	}
}
