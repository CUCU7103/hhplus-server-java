package kr.hhplus.be.server.global.support.lock.model;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import io.micrometer.observation.annotation.Observed;
import kr.hhplus.be.server.global.error.CustomErrorCode;
import kr.hhplus.be.server.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect // AOP Aspect임을 선언
@Component // 스프링 빈으로 등록
@RequiredArgsConstructor // final 필드 생성자 자동 생성
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 높은 우선순위 설정

@Slf4j
public class LockAspect {

	private final LockFactory lockFactory; // 락 팩토리 주입
	private final ExpressionParser parser = new SpelExpressionParser();
	private final ParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

	@Around("@annotation(WithLock)")
	@Observed(name = "withLock")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature)pjp.getSignature()).getMethod();
		WithLock withLock = method.getAnnotation(WithLock.class);

		// String resolvedKey = resolveKey(withLock.key(), method, pjp.getArgs());
		// log.error("KeyValue {} :", resolvedKey);
		LockStrategy lockStrategy = lockFactory.getLock(withLock.type());
		if (lockStrategy == null) {
			throw new IllegalArgumentException("No LockStrategy found for type: " + withLock.type());
		}

		LockContext context = switch (withLock.type()) {
			case REDIS_SIMPLE -> LockContext.createSimpleLockContext(withLock.key(), withLock.expireMillis());
			case REDIS_SPIN -> LockContext.createSpinLockContext(
				withLock.key(),
				withLock.timeoutMillis(),
				withLock.retryIntervalMillis(),
				withLock.expireMillis()
			);
			case REDIS_PUBSUB -> LockContext.createPubSubLockContext(
				withLock.key(),
				withLock.timeoutMillis(),
				withLock.expireMillis()
			);
			default -> throw new IllegalArgumentException("Unsupported lock type: " + withLock.type());
		};

		boolean acquired = lockStrategy.lock(context);
		if (!acquired) {
			log.error("Key : {} 에서 락 획득에 실패하였습니다.", withLock.key());
			throw new CustomException(CustomErrorCode.FAILED_ACQUIRE_LOCK);

		}

		try {
			return pjp.proceed();
		} finally {
			log.info("락을 해제합니다");
			lockStrategy.unlock(context);
		}
	}

	// private String resolveKey(String key, Method method, Object[] args) {
	// 	EvaluationContext context = new StandardEvaluationContext();
	// 	String[] paramNames = paramDiscoverer.getParameterNames(method);
	// 	if (paramNames != null) {
	// 		for (int i = 0; i < paramNames.length; i++) {
	// 			context.setVariable(paramNames[i], args[i]);
	// 		}
	// 	}
	// 	return parser.parseExpression(key).getValue(context, String.class);
	// }

}
