package kr.hhplus.be.server.global.kafka;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
	// @SpringBootTest에서 아래 두 프로퍼티를 주는 이유는, 테스트용 컨슈머가 기대한 메시지를 놓치지 않고, 오프셋 관리도 테스트 흐름에 방해되지 않도록 보장하기 위함입니다.

	properties = {
		// 1) 토픽의 맨 처음부터 읽기
		"spring.kafka.consumer.auto-offset-reset=earliest",
		// 2) 오프셋을 자동으로 커밋하지 않기
		"spring.kafka.consumer.enable-auto-commit=false"
	}
)
@ActiveProfiles("test")
public class KafkaTest {

	private static final String TOPIC = "test-topic";

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	// KafkaTemplate은 메시지 송신을 담당하는 스프링 빈입니다.
	// 제네릭 <키,값> 타입으로 직렬화/역직렬화 설정을 자동 적용하고,
	// .send() 호출로 카프카에 메시지를 보냅니다.

	@Autowired
	private ConsumerFactory<String, String> consumerFactory;
	// ConsumerFactory는 Consumer 인스턴스를 생성하는 팩토리 빈입니다.
	// 내부에 설정된 bootstrap-servers, deserializer 등을 바탕으로
	// 테스트용 컨슈머를 만들어 주며, 직접 구독·폴링에 사용합니다.

	// 테스트 클래스 상단에
	@BeforeEach
	void createTopic() throws Exception {
		// 1) 브로커 주소 가져오기 (TestcontainersConfiguration 에서 system property 로 주입된 값)
		String bootstrapServers = System.getProperty("spring.kafka.bootstrap-servers");

		// 2) AdminClient 설정 생성
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		// 3) AdminClient 생성 및 토픽 생성
		try (AdminClient admin = AdminClient.create(configs)) {
			NewTopic topic = new NewTopic(TOPIC, 1, (short)1);
			admin.createTopics(Collections.singletonList(topic))
				.all()
				.get(10, TimeUnit.SECONDS);   // 최대 10초 대기
		}
	}

	@Test
	void kafkaSendReceiveTest() {
		// 1) Consumer 생성 및 토픽 구독
		// group.id 와 clientIdSuffix("testClient") 를 직접 지정해서 생성
		Consumer<String, String> consumer =
			consumerFactory.createConsumer("test-group", "testClient");
		//    -> ConsumerFactory로부터 컨슈머 객체를 생성
		consumer.subscribe(Collections.singletonList(TOPIC));
		//    -> 지정한 토픽을 구독하도록 설정
		// 2) 메시지 전송 (flush()로 실제 전송 보장)
		String message = "Hello, Kafka!";
		kafkaTemplate.send(TOPIC, message);
		//    -> KafkaTemplate.send()로 메시지를 비동기 전송
		kafkaTemplate.flush();
		//    -> 내부 배치된 레코드를 강제로 전송해 테스트 시 타이밍 이슈 방지
		// 3) 메시지 수신 (getSingleRecord은 하나만 받았는지 검증)
		ConsumerRecord<String, String> received =
			KafkaTestUtils.getSingleRecord(consumer, TOPIC);
		//    -> KafkaTestUtils를 이용해 폴링, 단일 레코드를 반환
		// 4) 검증: 수신된 메시지 내용이 동일해야 합니다.
		assertThat(received.value()).isEqualTo(message);
		//    -> AssertJ로 송신한 값과 수신된 값이 일치하는지 확인
		// 5) 리소스 정리
		consumer.close();
	}
}
