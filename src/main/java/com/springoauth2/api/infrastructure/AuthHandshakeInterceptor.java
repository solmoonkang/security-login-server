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

	private final JwtProviderService jwtProviderService;

	@Override
	public boolean beforeHandshake(
		ServerHttpRequest serverHttpRequest,
		ServerHttpResponse serverHttpResponse,
		WebSocketHandler webSocketHandler,
		Map<String, Object> attributes
	) {
		log.info("[✅ LOGGER] START WEBSOCKET HANDSHAKE");

		ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest)serverHttpRequest;
		String accessToken = jwtProviderService.extractToken(
			ACCESS_TOKEN_HEADER, servletServerHttpRequest.getServletRequest());

		if (accessToken == null || !jwtProviderService.isUsable(accessToken)) {
			log.warn("[❎ LOGGER] JWT TOKEN IS INVALID OR NOT PRESENT");
			throw new NotFoundException("[❎ ERROR] INVALID OR MISSING JWT TOKEN");
		}

		AuthMember authMember = jwtProviderService.extractAuthMemberByAccessToken(accessToken);
		attributes.put("authMember", authMember);
		log.info("[✅ LOGGER] SUCCESS MEMBER AUTHORIZATION: {}", authMember.nickname());

		return true;
	}
}
