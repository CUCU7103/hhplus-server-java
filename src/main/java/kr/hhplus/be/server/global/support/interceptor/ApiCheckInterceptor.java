package kr.hhplus.be.server.global.support.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApiCheckInterceptor implements HandlerInterceptor {
	private static final String START_TIME = "API_START_TIME";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {

		long startTime = System.currentTimeMillis();
		request.setAttribute(START_TIME, startTime);

		if (handler instanceof HandlerMethod handlerMethod) {
			String controllerName = handlerMethod.getBeanType().getSimpleName(); // ex) BalanceController
			String methodName = handlerMethod.getMethod().getName();             // ex) getBalance
			log.info("=========== API 호출: {} {} → {}.{}() =======================",
				request.getMethod(), request.getRequestURI(), controllerName, methodName);
		} else {
			log.info("=========== API 호출: {} {} (Handler: {}) =======================",
				request.getMethod(), request.getRequestURI(), handler.getClass().getSimpleName());
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView) throws Exception {
		long startTime = (long)request.getAttribute(START_TIME);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		if (handler instanceof HandlerMethod handlerMethod) {
			String controllerName = handlerMethod.getBeanType().getSimpleName();
			String methodName = handlerMethod.getMethod().getName();
			log.info("=================== API 완료: {} {} → {}.{}() =========================",
				request.getMethod(), request.getRequestURI(), controllerName, methodName);
		} else {
			log.info("=================== API 완료: {} {} =========================",
				request.getMethod(), request.getRequestURI());
		}
	}

}
