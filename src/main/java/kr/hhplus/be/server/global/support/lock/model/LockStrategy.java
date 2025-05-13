package kr.hhplus.be.server.global.support.lock.model;

// 락 구현제 에서 사용할 메서드 정의

public interface LockStrategy {

	// LockContext 생성 메서드 추가
	LockContext createContext(String key, WithLock lockAnnotation);

	// 락 획득 메서드
	boolean lock(LockContext context);

	// 락 해제 메서드
	void unlock(LockContext context);

}
