package kr.hhplus.be.server.infrastructure.token;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;

public interface TokenJpaRepository extends JpaRepository<Token, Long> {

	Optional<Token> findByUserIdAndStatus(long userId, TokenStatus status);

	Optional<Token> findById(long tokenId);

	Optional<Token> findByUserId(long userId);

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM Token t WHERE t.id = :id")
	Optional<Token> findTokenWithWriteLock(@Param("id") long tokenId);

}
