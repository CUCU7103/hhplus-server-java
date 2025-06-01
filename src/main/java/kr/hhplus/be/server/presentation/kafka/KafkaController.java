package kr.hhplus.be.server.presentation.kafka;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.application.kafka.DemoProducer;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka")
public class KafkaController {
	private final DemoProducer producer;

	@PostMapping("/publish/1")
	public ResponseEntity<Void> publish1(@RequestParam String msg) {
		producer.sendMessage1("demo-topic1", msg);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/publish/2")
	public ResponseEntity<Void> publish2(@RequestParam String msg) {
		producer.sendMessage2("demo-topic2", msg);
		return ResponseEntity.ok().build();
	}

}
