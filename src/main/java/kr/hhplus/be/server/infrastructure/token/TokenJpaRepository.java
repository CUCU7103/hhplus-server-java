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

	@Query(value = """
		SELECT id FROM tokens
		WHERE status = 'ACTIVE'
		ORDER BY id ASC
		LIMIT 1
		LOCK IN SHARE MODE
		""", nativeQuery = true)
	Optional<Long> lockAnyActiveTokenId();

	@Query(value = """
		SELECT COUNT(*) FROM tokens
		WHERE status = 'WAITING'
		  AND created_at < (
		    SELECT created_at FROM (
		      SELECT created_at FROM tokens WHERE id = :id LOCK IN SHARE MODE
		    ) AS sub
		  )
		""", nativeQuery = true)
	int getWaitingRankWithSharedLock(@Param("id") long tokenId);

	long countByStatus(TokenStatus tokenStatus);

	List<Token> findAllByStatus(TokenStatus tokenStatus);

	Optional<Token> findByIdAndStatus(long userId, TokenStatus status);

	@Lock(LockModeType.PESSIMISTIC_READ) // 공유 락 사용
	@Query("SELECT t FROM Token t WHERE t.id = :id AND t.status = :status")
	Optional<Token> findTokenForUpdate(@Param("id") long tokenId, @Param("status") TokenStatus status);

}
