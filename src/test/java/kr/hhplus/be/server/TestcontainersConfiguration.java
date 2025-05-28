package kr.hhplus.be.server;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import jakarta.annotation.PreDestroy;

@Configuration
class TestcontainersConfiguration {

	public static final MySQLContainer<?> MYSQL_CONTAINER;

	static {
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
			.withDatabaseName("hhplus")
			.withUsername("test")
			.withPassword("test");
		MYSQL_CONTAINER.start();

		System.setProperty("spring.datasource.url",
			MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
	}

	// --- Redis 컨테이너 (추가) ---
	public static final GenericContainer<?> REDIS_CONTAINER;

	static {
		REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.0.5-alpine"))
			.withExposedPorts(6379)
			.withCreateContainerCmdModifier(cmd ->
				cmd.withHostConfig(
					new HostConfig()
						.withPortBindings(
							new PortBinding(
								Ports.Binding.bindPort(6380),
								new ExposedPort(6379)
							)
						)
				)
			);
		REDIS_CONTAINER.start();

		// Spring Data Redis
		System.setProperty("spring.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.redis.port", REDIS_CONTAINER.getFirstMappedPort().toString());
		// Redisson Spring Boot Starter 우선순위가 높은 single-server 설정
		System.setProperty(
			"spring.redisson.single-server-config.address",
			String.format("redis://%s:%d",
				REDIS_CONTAINER.getHost(),
				REDIS_CONTAINER.getFirstMappedPort()
			)
		);
	}

	// --- Kafka 컨테이너 (추가) ---
	public static final KafkaContainer KAFKA_CONTAINER;

	static {

		KAFKA_CONTAINER = new KafkaContainer(
			DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
		);

		KAFKA_CONTAINER.start();

		// 3) Spring Boot에 bootstrap-servers 주소 주입
		System.setProperty(
			"spring.kafka.bootstrap-servers",
			KAFKA_CONTAINER.getBootstrapServers()
		);
	}

	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}
		if (REDIS_CONTAINER.isRunning()) {
			REDIS_CONTAINER.stop();
		}
		if (KAFKA_CONTAINER.isRunning()) {
			KAFKA_CONTAINER.stop();
		}
	}
}
