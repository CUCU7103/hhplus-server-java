package kr.hhplus.be.server.domain.user;

import java.util.Optional;

public interface UserRepository {
	Optional<User> findById(long userId);
}
