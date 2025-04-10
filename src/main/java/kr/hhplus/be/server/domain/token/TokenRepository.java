package kr.hhplus.be.server.domain.token;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository {
	Token getToken(long userId);

	boolean existsById(long userId);

	Optional<Token> findByUserId(long userId);

	void save(Token token);
}
