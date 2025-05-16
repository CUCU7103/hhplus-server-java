package kr.hhplus.be.server.infrastructure.token;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;

public interface TokenJpaRepository {

	Optional<Token> findByUserIdAndStatus(long userId, TokenStatus status);

	Optional<Token> findById(long tokenId);

	Optional<Token> findByUserId(long userId);

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);

	Optional<Token> findTokenWithWriteLock(@Param("id") long tokenId);

}
