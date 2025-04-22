package kr.hhplus.be.server.domain.token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository {

	Optional<Token> findToken(long tokenId);

	Optional<Token> findTokenIdAndWaitingToken(long tokenId);

	Optional<Token> findByUserIdAndWaitingToken(long userId);

	Optional<Token> findByUserId(long userId);

	int getWaitingRankWithSharedLock(long id);

	Optional<Long> lockAnyActiveTokenId();

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);

	Optional<Token> findTokenWithSharedLock(long tokenId);
}
