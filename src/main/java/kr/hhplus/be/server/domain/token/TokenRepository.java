package kr.hhplus.be.server.domain.token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository {

	Optional<Token> findByUserIdAndWaitingToken(long userId);

	Optional<Token> findByUserId(long userId);

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);

	Optional<Token> findTokenWithWriteLock(long tokenId);

}
