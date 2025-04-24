package kr.hhplus.be.server.presentation.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.application.token.TokenService;
import kr.hhplus.be.server.global.support.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
public class TokenController {

	private final TokenService tokenService;

	/**
	 * 유저 대기열 토큰 발급 API
	 *
	 */
	// @PathVariable(name = "userId") long userId
	@PostMapping("/issue")
	public ResponseEntity<TokenResponse> issueToken(@CurrentUserId Long userId) {
		return ResponseEntity.ok()
			.body(TokenResponse.from("토큰 발급 성공", tokenService.issueToken(userId)));
	}

	/**
	 * 대기열 조회 API
	 *  차례가 되어 토큰이 활성화 된 부분을 확인
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<TokenSearchResponse> searchToken(@PathVariable long userId) {
		return ResponseEntity.ok()
			.body(TokenSearchResponse.from("대기열 조회 성공", tokenService.searchToken(userId)));
	}

	// 토큰 활성화 엔드포인트 (POST)
	@PostMapping("/{tokenId}/activate")
	public ResponseEntity<TokenActiveResponse> activateToken(@PathVariable(name = "tokenId") long tokenId) {
		return ResponseEntity.ok()
			.body(TokenActiveResponse.from("토큰 활성화 성공", tokenService.activateToken(tokenId)));
	}

}
