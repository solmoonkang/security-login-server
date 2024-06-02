package com.springoauth2.api.infrastructure;

import static com.springoauth2.global.util.Constant.*;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.springoauth2.api.application.auth.JwtProviderService;
import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuthHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

	/**
	 * AuthHandshakeInterceptor: 웹소켓 핸드셰이크 과정에서 JWT 토큰을 검증하여 사용자 인증을 처리하는 인터셉터
	 * 웹소켓 연결이 수립되기 전에 JWT 토큰을 확인하고, 유효한 경우 사용자의 인증 정보를 연결에 포함
	 * 웹소켓 연결을 시작하기 전에 JWT 토큰을 통해 사용자를 인증하는 역할
	 * 이를 통해 인증되지 않은 사용자의 접근을 차단하고, 인증된 사용자만이 웹소켓 연결을 수립
	 */

	private final JwtProviderService jwtProviderService;

	/**
	 * beforeHandshake: 웹소켓 핸드셰이크가 시작되기 전에 호출 (핸드셰이크는 클라이언트가 서버에 웹소켓 연결을 요청할 때 수행되는 과정)
	 */
	@Override
	public boolean beforeHandshake(
		ServerHttpRequest serverHttpRequest,
		ServerHttpResponse serverHttpResponse,
		WebSocketHandler webSocketHandler,
		Map<String, Object> attributes
	) {
		ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest)serverHttpRequest;
		String accessToken = jwtProviderService.extractToken(ACCESS_TOKEN_HEADER,
			servletServerHttpRequest.getServletRequest());

		if (accessToken == null) {
			log.warn("❎[ERROR] JWT 토큰이 요청에 포함되어 있지 않습니다.");
			throw new NotFoundException("JWT 토큰이 존재하지 않습니다.");
		}

		if (!jwtProviderService.isUsable(accessToken)) {
			log.warn("❎[ERROR] JWT 토큰이 유효하지 않습니다.");
			throw new NotFoundException("유효하지 않은 JWT 토큰입니다.");
		}

		AuthMember authMember = jwtProviderService.extractAuthMemberByAccessToken(accessToken);
		attributes.put("authMember", authMember);
		log.info("✅[SUCCESS] 사용자 인증 성공: {}", authMember.nickname());

		return true;
	}
}
