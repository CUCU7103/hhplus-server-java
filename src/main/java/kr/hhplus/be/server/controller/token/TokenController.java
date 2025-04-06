package kr.hhplus.be.server.controller.token;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.config.swagger.TokenApi;

@RestController
@RequestMapping("/api/v1/token")
public class TokenController implements TokenApi {

	/**
	 * [MOCK] 유저 대기열 토큰 발급 API
	 *
	 */
	@PostMapping("/{userId}/issue")
	public ResponseEntity<TokenResponse> issueToken(@PathVariable(name = "userId") long userId) {

		// 실제 서비스의 호출 없이 , 고정 응답을 반환합니다.
		TokenInfo mockInfo = TokenInfo.builder()
			.tokenValue(UUID.randomUUID().toString())
			.status(TokenStatus.WAITING)
			.createdAt(LocalDateTime.now())
			.userId(userId)
			.build();

		TokenResponse response = new TokenResponse("토큰 생성 성공", mockInfo);
		return ResponseEntity.ok().body(response);
	}

	/**
	 * [MOCK] 대기열 조회 API
	 *  차례가 되어 토큰이 활성화 된 부분을 확인
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<TokenResponse> searchToken(@PathVariable(name = "userId") long userId) {

		// 실제 서비스의 호출 없이 , 고정 응답을 반환합니다.
		TokenSearchInfo mockInfo = TokenSearchInfo.builder()
			.tokenValue(UUID.randomUUID().toString())
			.status(TokenStatus.ACTIVE)
			.userId(userId)
			.createdAt(LocalDateTime.now())
			.updateAt(LocalDateTime.now().plusMinutes(2))
			.build();

		TokenResponse response = new TokenResponse("대기열 순서 조회 성공", mockInfo);
		return ResponseEntity.ok().body(response);
	}

}
