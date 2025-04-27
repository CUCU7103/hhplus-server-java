package kr.hhplus.be.server.global.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.server.global.support.filter.ApiFilter;

@Configuration
public class FilterConfig {
	@Bean
	public FilterRegistrationBean<ApiFilter> apiFilter() {
		FilterRegistrationBean<ApiFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new ApiFilter());
		registrationBean.addUrlPatterns("/*");
		registrationBean.setOrder(1);
		return registrationBean;
	}

}
