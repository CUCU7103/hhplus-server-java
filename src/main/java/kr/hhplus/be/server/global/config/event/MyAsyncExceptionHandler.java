package kr.hhplus.be.server.global.config.event;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		log.error("### @Async 예외 발생 ### method={}, params={}, error={}",
			method.getName(), Arrays.toString(params), ex.getMessage(), ex);
	}
}
