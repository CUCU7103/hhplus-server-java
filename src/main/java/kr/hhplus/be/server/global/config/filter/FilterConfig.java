package kr.hhplus.be.server.global.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.tracing.Tracer;
import kr.hhplus.be.server.global.support.filter.ApiFilter;

@Configuration
public class FilterConfig {
	private final Tracer tracer;

	public FilterConfig(Tracer tracer) {
		this.tracer = tracer;
	}

	@Bean
	public FilterRegistrationBean<ApiFilter> apiFilter() {
		FilterRegistrationBean<ApiFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new ApiFilter(tracer));
		registrationBean.addUrlPatterns("/*");
		registrationBean.setOrder(1);
		return registrationBean;
	}

}
