package kr.hhplus.be.server.infrastructure.token;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;

public interface TokenJpaRepository extends JpaRepository<Token, Long> {

	Optional<Token> findByUserIdAndStatus(long userId, TokenStatus status);

	Optional<Token> findById(long tokenId);

	Optional<Token> findByUserId(long userId);

	@Query("SELECT COUNT(t) FROM Token t WHERE t.status = 'WAITING' AND t.createdAt < (SELECT t2.createdAt FROM Token t2 WHERE t2.id = :tokenId)")
	int getWaitingRank(@Param("tokenId") long tokenId);

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);

	Optional<Token> findByIdAndStatus(long userId, TokenStatus status);

}
