package kr.hhplus.be.server.global.support.lock.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메소드 레벨에서 사용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 애노테이션 유지
public @interface WithLock {

	String key(); // 락 식별 키 값

	LockType type() default LockType.REDIS_SPIN; // 기본 락 타입 설정 (Spin Lock)

	long timeoutMillis() default 3000; // 락 획득을 시도할 최대 대기 시간 제한( 3초)

	long retryIntervalMillis() default 100; // 기본 재시도 간격 (밀리초)

	long expireMillis() default 5000; // 락 만료 시간

}
