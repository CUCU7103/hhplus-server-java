package kr.hhplus.be.server.global.config.web;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import kr.hhplus.be.server.global.support.interceptor.ApiCheckInterceptor;
import kr.hhplus.be.server.global.support.interceptor.TokenInterceptor;
import kr.hhplus.be.server.global.support.resolver.CurrentUserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {
	private final TokenInterceptor tokenInterceptor;
	private final ApiCheckInterceptor apiCheckInterceptor;
	private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(tokenInterceptor)
			.addPathPatterns("/api/v1/concerts/**", "/api/v1/payments/**",
				"/api/v1/reservations/**"); // 토큰 검증이 필요한 API 경로 설정
		registry.addInterceptor(apiCheckInterceptor).addPathPatterns("/api/**");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(currentUserIdArgumentResolver);
	}

}
