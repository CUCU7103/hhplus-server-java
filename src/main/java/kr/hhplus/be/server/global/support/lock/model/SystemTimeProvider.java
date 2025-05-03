package kr.hhplus.be.server.global.support.lock.model;

import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {
	@Override
	public long currentTime() {
		return System.currentTimeMillis();
	}
}
