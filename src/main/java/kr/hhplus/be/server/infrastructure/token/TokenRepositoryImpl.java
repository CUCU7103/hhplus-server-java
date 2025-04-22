package kr.hhplus.be.server.infrastructure.token;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {
	private final TokenJpaRepository tokenJpaRepository;

	@Override
	public Optional<Token> findToken(long tokenId) {
		return tokenJpaRepository.findById(tokenId);
	}

	@Override
	public Optional<Token> findByUserId(long userId) {
		return tokenJpaRepository.findByUserId(userId);
	}

	@Override
	public int getWaitingRankWithSharedLock(long id) {
		return tokenJpaRepository.getWaitingRankWithSharedLock(id);
	}

	@Override
	public Optional<Long> lockAnyActiveTokenId() {
		return tokenJpaRepository.lockAnyActiveTokenId();
	}

	@Override
	public long countByStatus(TokenStatus tokenStatus) {
		return tokenJpaRepository.countByStatus(tokenStatus);
	}

	@Override
	public List<Token> findAllByStatus(TokenStatus tokenStatus) {
		return tokenJpaRepository.findAllByStatus(tokenStatus);
	}

	@Override
	public Optional<Token> findTokenIdAndWaitingToken(long tokenId) {
		return tokenJpaRepository.findByIdAndStatus(tokenId, TokenStatus.WAITING);
	}

	@Override
	public Optional<Token> findByUserIdAndWaitingToken(long userId) {
		return tokenJpaRepository.findByUserIdAndStatus(userId, TokenStatus.WAITING);
	}

	@Override
	public Optional<Token> findTokenWithSharedLock(long tokenId) {
		return tokenJpaRepository.findTokenForUpdate(tokenId, TokenStatus.WAITING);
	}
}
