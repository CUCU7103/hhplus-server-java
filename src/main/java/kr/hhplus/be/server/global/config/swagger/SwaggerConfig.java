package kr.hhplus.be.server.global.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		Info info = new Info()
			.title("Swagger Test")
			.version("0.0.1")
			.description("<h3>Swagger test</h3>");

		return new OpenAPI().info(info);
	}
}
