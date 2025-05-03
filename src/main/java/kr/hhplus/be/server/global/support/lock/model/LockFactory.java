package kr.hhplus.be.server.global.support.lock.model;

public interface LockFactory {
	LockStrategy getLock(LockType type);
}
