package kr.hhplus.be.server.domain.token;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

public interface TokenRepository {

	Optional<Token> findToken(long userId);

	Optional<Token> findByUserId(long userId);

	@Query("SELECT COUNT(t) FROM Token t WHERE t.status = 'WAITING' AND t.createdAt < (SELECT t2.createdAt FROM Token t2 WHERE t2.id = :tokenId)")
	int getWaitingRank(long id);

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);
}
