package kr.hhplus.be.server.presentation.deploytest;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deploy")
@RequiredArgsConstructor
@Validated
public class DeployTestController {

	@GetMapping("/")
	public String deployTest() {
		return "Successful deploy";
	}
}
