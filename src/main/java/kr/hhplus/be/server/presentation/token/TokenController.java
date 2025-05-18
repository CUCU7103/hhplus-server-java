package kr.hhplus.be.server.presentation.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	 */
	// @PathVariable(name = "userId") long userId
	@PostMapping("/issue")
	public ResponseEntity<TokenResponse> issueToken(@CurrentUserId Long userId) {
		return ResponseEntity.ok()
			.body(TokenResponse.from("토큰 발급 성공", tokenService.issueToken(userId)));
	}

	@GetMapping("/rank")
	public ResponseEntity<TokenSearchResponse> searchTokenRank(@CurrentUserId Long userId) {
		return ResponseEntity.ok()
			.body(TokenSearchResponse.from("토큰 발급 성공", tokenService.searchTokenRank(userId)));
	}

}
