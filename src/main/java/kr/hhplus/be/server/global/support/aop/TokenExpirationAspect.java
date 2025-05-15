package kr.hhplus.be.server.global.support.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import kr.hhplus.be.server.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class TokenExpirationAspect {

	private final TokenRepository tokenRepository;
	private final PlatformTransactionManager transactionManager;

/*	@AfterReturning(
		pointcut = "@annotation(ExpireTokenAfterCommit) && args(reservationId, userId, ..)",
		returning = "ret",
		argNames = "reservationId,userId,ret")
	public void afterPayment(long reservationId, long userId, Object ret) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(
				new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						// 새로운 트랜잭션에서 토큰 만료 처리
						DefaultTransactionDefinition def = new DefaultTransactionDefinition();
						def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
						TransactionStatus status = transactionManager.getTransaction(def);

						try {
							tokenRepository.findByUserId(userId).ifPresent(token -> {
								token.expiredToken();
								tokenRepository.save(token);
								log.debug("Token expired for userId={}", userId);
							});
							transactionManager.commit(status);
						} catch (Exception e) {
							transactionManager.rollback(status);
							log.error("Failed to expire token for userId={}: {}", userId, e.getMessage());
						}
					}
				}
			);
		}
	}*/

}


