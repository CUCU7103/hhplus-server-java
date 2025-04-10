package kr.hhplus.be.server.interfaces.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.application.TokenService;
import kr.hhplus.be.server.global.config.swagger.TokenApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController implements TokenApi {

	private final TokenService tokenService;

	/**
	 * [MOCK] 유저 대기열 토큰 발급 API
	 *
	 */
	@PostMapping("/{userId}/issue")
	public ResponseEntity<TokenResponse> issueToken(@PathVariable(name = "userId") long userId) {
		return ResponseEntity.ok()
			.body(TokenResponse.from("토큰 발급 성공", tokenService.issueToken(userId)));
	}

	/**
	 * [MOCK] 대기열 조회 API
	 *  차례가 되어 토큰이 활성화 된 부분을 확인
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<TokenSearchResponse> searchToken(@PathVariable(name = "userId") long userId) {
		return ResponseEntity.ok()
			.body(TokenSearchResponse.from("대기열 조회 성공", tokenService.searchToken(userId)));
	}

}
