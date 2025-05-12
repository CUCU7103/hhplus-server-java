package kr.hhplus.be.server.global.support.lock.model;

public enum LockType {
	REDIS_SPIN,
	REDIS_PUBSUB,
	REDIS_SIMPLE;
}
