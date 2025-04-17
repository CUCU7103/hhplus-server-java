package kr.hhplus.be.server.domain.token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository {

	Optional<Token> findToken(long tokenId);

	Optional<Token> findTokenIdAndWaitingToken(long tokenId);

	Optional<Token> findByUserIdAndWaitingToken(long userId);

	Optional<Token> findByUserId(long userId);

	int getWaitingRank(long id);

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);
}
