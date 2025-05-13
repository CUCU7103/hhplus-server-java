package kr.hhplus.be.server.global.support.lock.model;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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
	private static final String LOCK_PREFIX = "lock:";

	@Around("@annotation(WithLock)")
	@Observed(name = "withLock")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature)pjp.getSignature()).getMethod();
		WithLock withLock = method.getAnnotation(WithLock.class);
		LockStrategy lockStrategy = lockFactory.getLock(withLock.type());

		if (lockStrategy == null) {
			throw new IllegalArgumentException("No LockStrategy found for type: " + withLock.type());
		}

		String lockKey = lockKeyParser(withLock.key(), method, pjp.getArgs());
		log.info("Lock key {}", lockKey);

		// LockContext 생성
		LockContext context = lockStrategy.createContext(lockKey, withLock);
		boolean acquired = false;

		try {
			// 락 획득 시도 및 예외 처리
			acquired = lockStrategy.lock(context);
			if (!acquired) {
				log.error("Key : {} 에서 락 획득에 실패하였습니다.", lockKey);
				throw new CustomException(CustomErrorCode.FAILED_ACQUIRE_LOCK);
			}
			// 실제 비즈니스 로직 실행
			return pjp.proceed();
		} catch (InterruptedException e) {
			log.error("Key : {} 락 획득 중 인터럽트가 발생하였습니다.", lockKey, e);
			Thread.currentThread().interrupt(); // 인터럽트 상태 복원
			throw new CustomException(CustomErrorCode.FAILED_ACQUIRE_LOCK);
		} catch (Exception e) {
			log.error("Key : {} 락 처리 중 예외가 발생하였습니다.", lockKey, e);
			throw e;
		} finally {
			// 락 해제 시 예외 처리
			if (acquired) {
				try {
					lockStrategy.unlock(context);
					log.info("락을 해제하였습니다. Key: {}", lockKey);
				} catch (Exception e) {
					log.error("Key : {} 락 해제 중 예외가 발생하였습니다.", lockKey, e);
					// 락 해제 실패는 로깅만 하고 상위로 전파하지 않음
				}
			}
		}
	}

	// spel 파싱 진행
	private String lockKeyParser(String lockKey, Method method, Object[] args) {

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext();

		Parameter[] parameters = method.getParameters();

		for (int i = 0; i < parameters.length; i++) {
			context.setVariable(parameters[i].getName(), args[i]);
		}

		Expression expression = parser.parseExpression(lockKey);
		return LOCK_PREFIX + expression.getValue(context, String.class);
	}

}
