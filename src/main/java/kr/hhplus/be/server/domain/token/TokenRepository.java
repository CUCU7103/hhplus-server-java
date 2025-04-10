package kr.hhplus.be.server.domain.token;

import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository {
	Token getToken(long userId);
}
