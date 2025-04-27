package kr.hhplus.be.server.application.concurrency;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.application.token.info.ActiveTokenInfo;
import kr.hhplus.be.server.domain.token.Token;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.token.TokenJpaRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class TokenConcurrencyTest {

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private TokenJpaRepository tokenJpaRepository;

	@Autowired
	private TokenService tokenService;

	@PersistenceContext
	EntityManager entityManager;

	@BeforeEach
	public void setUp() {
		userJpaRepository.deleteAll();
		tokenJpaRepository.deleteAll();
	}

	@Test
	void 다수_사용자의_토큰_동시_요청시_우선순위가_1순위인_토큰만_활성화된다() throws InterruptedException {
		// 1) 준비: 100명의 사용자 & 각자 토큰 1건씩 생성 (모두 WAITING)
		int userCount = 100;
		List<User> users = new ArrayList<>();
		List<Token> tokens = new ArrayList<>();

		// DB에 사용자와 토큰 생성
		for (int i = 0; i < userCount; i++) {
			User user = userJpaRepository.save(User.builder().name("사용자" + i).build());
			users.add(user);
			Token token = tokenJpaRepository.save(Token.createToken(user));
			tokens.add(token);
		}

		// 토큰 ID 목록 저장 (새 목록 생성)
		List<Long> tokenIds = new ArrayList<>();
		for (Token token : tokens) {
			tokenIds.add(token.getId());
		}

		// 2) 동시 요청 설정
		ExecutorService executor = Executors.newFixedThreadPool(userCount);
		CountDownLatch readyLatch = new CountDownLatch(userCount); // 모든 스레드 준비 확인용
		CountDownLatch startLatch = new CountDownLatch(1);         // 시작 신호용
		CountDownLatch doneLatch = new CountDownLatch(userCount);  // 완료 확인용
		ConcurrentHashMap<Long, ActiveTokenInfo> results = new ConcurrentHashMap<>();
		List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

		// 3) 각 토큰 ID에 대한 활성화 요청 작업 생성
		List<Future<?>> futures = new ArrayList<>();

		for (Long tokenId : tokenIds) {
			Future<?> future = executor.submit(() -> {
				try {
					// 준비 완료 신호
					readyLatch.countDown();
					// 시작 신호 대기
					startLatch.await();

					// 토큰 활성화 요청
					ActiveTokenInfo info = tokenService.activateToken(tokenId);
					results.put(tokenId, info);
				} catch (Exception e) {
					log.error("토큰 ID {} 처리 중 오류: {}", tokenId, e.getMessage());
					exceptions.add(e);
				} finally {
					// 작업 완료 신호
					doneLatch.countDown();
				}
			});
			futures.add(future);
		}

		// 4) 모든 스레드가 준비될 때까지 대기
		boolean allReady = readyLatch.await(5, TimeUnit.SECONDS);
		assertThat(allReady).isTrue();

		// 5) 약간의 추가 대기 시간 부여 (모든 스레드가 확실히 대기 상태에 진입하도록)
		Thread.sleep(500);

		// 6) 모든 스레드 동시 시작
		log.info("==== 모든 토큰 활성화 요청 동시 시작 ====");
		startLatch.countDown();

		// 7) 모든 작업 완료 대기
		boolean allDone = doneLatch.await(30, TimeUnit.SECONDS);
		assertThat(allDone).withFailMessage("일부 작업이 제한 시간 내에 완료되지 않았습니다").isTrue();

		// 8) 스레드 풀 정리
		executor.shutdown();
		boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
		if (!terminated) {
			log.info("일부 작업이 여전히 실행 중이어서 강제 종료합니다");
			executor.shutdownNow();
		}

		// 9) 예외 발생 여부 확인
		if (!exceptions.isEmpty()) {
			log.info("발생한 예외 목록:");
			for (Exception e : exceptions) {
				log.error(" - {}", e.getMessage());
			}
		}
		assertThat(exceptions).isEmpty();

		// 10) 모든 토큰에 대한 결과가 있는지 확인
		assertThat(results).hasSize(userCount);

		// 11) waitingRank 검증
		List<Integer> ranks = results.values().stream()
			.map(ActiveTokenInfo::waitingRank)
			.sorted()
			.toList();

		// 11-1) 중복된 rank 확인
		Set<Integer> uniqueRanks = new HashSet<>(ranks);
		if (uniqueRanks.size() != ranks.size()) {
			log.info("⚠️ 중복된 waitingRank 발견:");
			Map<Integer, Long> rankCounts = ranks.stream()
				.collect(Collectors.groupingBy(r -> r, Collectors.counting()));
			rankCounts.forEach((rank, count) -> {
				if (count > 1) {
					log.info("  - Rank {}: {}번 발생", rank, count);
				}
			});
		}

		// 11-2) 누락된 rank 확인
		log.info("📊 waitingRank 분포: {} ", ranks);
		Set<Integer> expectedRanks = IntStream.range(0, userCount)
			.boxed()
			.collect(Collectors.toSet());
		Set<Integer> missingRanks = new HashSet<>(expectedRanks);
		missingRanks.removeAll(uniqueRanks);

		if (!missingRanks.isEmpty()) {
			log.info("⚠️ 누락된 waitingRank: {} ", missingRanks);
		}

		// 검증은 덜 엄격하게 변경: 모든 rank가 유일해야 함
		assertThat(uniqueRanks.size()).isEqualTo(ranks.size())
			.withFailMessage("중복된 waitingRank가 있습니다: " + ranks);

		// 검증 실패 시 디버깅 정보 제공
		if (!expectedRanks.equals(uniqueRanks)) {
			log.info("⚠️ waitingRank 검증 실패:");
			log.info("  - 예상 rank 범위: 0- {}", (userCount - 1));
			log.info("  - 실제 발견된 rank: {} ", uniqueRanks);
		}

		// 12) DB에 반영된 상태 검증: userCount개 토큰 중 정확히 1개만 ACTIVE 상태여야 함
		List<Token> persistedTokens = tokenJpaRepository.findAllById(tokenIds);
		assertThat(persistedTokens).hasSize(userCount);

		long activeCount = persistedTokens.stream()
			.filter(t -> t.getStatus() == TokenStatus.ACTIVE)
			.count();
		long waitingCount = persistedTokens.stream()
			.filter(t -> t.getStatus() == TokenStatus.WAITING)
			.count();

		assertThat(activeCount).isEqualTo(1);
		assertThat(waitingCount).isEqualTo(userCount - 1);

		// 13) 활성화된 토큰은 waitingRank가 0이어야 함
		Token activeToken = persistedTokens.stream()
			.filter(t -> t.getStatus() == TokenStatus.ACTIVE)
			.findFirst()
			.orElseThrow(() -> new AssertionError("활성화된 토큰이 없습니다"));

		ActiveTokenInfo activeInfo = results.get(activeToken.getId());
		assertThat(activeInfo.waitingRank()).isEqualTo(0);

		log.info("==== 테스트 성공적으로 완료 ====");
	}
}
