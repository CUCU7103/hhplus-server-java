package kr.hhplus.be.server.global.support.lock.model;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

	@Around("@annotation(WithLock)")
	@Observed(name = "withLock")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature)pjp.getSignature()).getMethod();
		WithLock withLock = method.getAnnotation(WithLock.class);
		LockStrategy lockStrategy = lockFactory.getLock(withLock.type());

		if (lockStrategy == null) {
			throw new IllegalArgumentException("No LockStrategy found for type: " + withLock.type());
		}
		// 변경된 부분: LockContext 생성 책임을 LockStrategy에 위임
		LockContext context = lockStrategy.createContext(withLock.key(), withLock);
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

}
