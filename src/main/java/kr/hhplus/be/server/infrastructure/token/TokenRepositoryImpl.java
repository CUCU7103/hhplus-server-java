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
	public Token getToken(long userId) {
		return tokenJpaRepository.getToken(userId);
	}

	@Override
	public Optional<Token> findByUserId(long userId) {
		return tokenJpaRepository.findByUserId(userId);
	}

	@Override
	public int getWaitingRank(long id) {
		return tokenJpaRepository.getWaitingRank(id);
	}

	@Override
	public long countByStatus(TokenStatus tokenStatus) {
		return tokenJpaRepository.countByStatus(tokenStatus);
	}

	@Override
	public List<Token> findAllByStatus(TokenStatus tokenStatus) {
		return tokenJpaRepository.findAllByStatus(tokenStatus);
	}
}
