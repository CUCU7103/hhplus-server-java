package kr.hhplus.be.server.application.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.infrastructure.token.TokenJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class TokenIntegrationTest {

	@Autowired
	private TokenService tokenService;
	@Autowired
	private UserJpaRepository userJpaRepository;
	@Autowired
	private TokenJpaRepository tokenJpaRepository;

/*
	@Test
	void 대기_토큰이_없는_사용자는_토큰_발급에_성공한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("김씨").build());
		// act
		IssueTokenInfo info = tokenService.issueToken(user.getId());

		// assert
		assertThat(info).isNotNull();
		assertThat(info.userId()).isEqualTo(user.getId());
		assertThat(info.status()).isEqualTo(TokenStatus.WAITING);
		assertDoesNotThrow(() -> UUID.fromString(info.tokenValue()));
	}

	@Test
	void 대기_토큰을_보유한_사용자에게는_보유한_토큰을_반환한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("창씨").build());
		Token token = tokenJpaRepository.save(Token.createToken(user));
		// act
		IssueTokenInfo info = tokenService.issueToken(user.getId());

		// assert
		assertThat(info).isNotNull();
		assertThat(info.tokenValue()).isEqualTo(token.getTokenValue());
		assertThat(info.userId()).isEqualTo(token.getUser().getId());
		assertThat(info.status()).isEqualTo(token.getStatus());

	}

	@Test
	void 존재하는_사용자의_대기열_토큰_조회에_성공한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("박씨").build());
		Token token = tokenJpaRepository.save(Token.createToken(user));
		log.error("token: {}", token);
		// act
		SearchTokenInfo info = tokenService.searchToken(user.getId());
		// assert
		assertThat(info).isNotNull();
		assertThat(info.userId()).isEqualTo(user.getId());
		assertThat(info.status()).isEqualTo(TokenStatus.WAITING);
		assertDoesNotThrow(() -> UUID.fromString(info.tokenValue()));
	}

	@Test
	void 토큰의_우선순위가_1순위이고_최대활성토큰_수를_초과하지_않으면_토큰_활성화에_성공한다() {
		// arrange
		User user = userJpaRepository.save(User.builder().name("김씨").build());
		Token token = tokenJpaRepository.save(Token.createToken(user));
		// act
		ActiveTokenInfo info = tokenService.activateToken(token.getId());
		// assert
		assertThat(info).isNotNull();
		assertThat(info.userId()).isEqualTo(user.getId());
		assertThat(info.waitingRank() + 1).isEqualTo(1);
		assertThat(info.status()).isEqualTo(TokenStatus.ACTIVE);
	}

	@Test
	void 스케줄러가_동작하면_만료된_토큰만_EXPIRED로_변경된다() {

		User user = userJpaRepository.save(User.builder().name("김씨").build());
		Token token1 = tokenJpaRepository.save(Token.createToken(user));
		Token token2 = tokenJpaRepository.save(Token.createToken(user));
		Token token3 = tokenJpaRepository.save(Token.createToken(user));
		ReflectionTestUtils.setField(token1, "status", TokenStatus.ACTIVE);
		ReflectionTestUtils.setField(token1, "expirationAt", LocalDateTime.now().minusMinutes(10));
		ReflectionTestUtils.setField(token2, "status", TokenStatus.ACTIVE);
		ReflectionTestUtils.setField(token2, "expirationAt", LocalDateTime.now().minusMinutes(10));
		ReflectionTestUtils.setField(token3, "status", TokenStatus.ACTIVE);
		ReflectionTestUtils.setField(token3, "expirationAt", LocalDateTime.now().plusMinutes(10));
		// 2. 메서드 직접 호출 (스케줄러 대신)
		tokenService.expireTokensScheduler();

		// 3. DB에서 재조회
		List<Token> all = tokenJpaRepository.findAll();
		Map<TokenStatus, Long> statusCount = all.stream()
			.collect(Collectors.groupingBy(Token::getStatus, Collectors.counting()));

		// 4. 검증
		assertThat(statusCount.get(TokenStatus.EXPIRED)).isEqualTo(2L);
		assertThat(statusCount.get(TokenStatus.ACTIVE)).isEqualTo(1L);
	}
*/

}

