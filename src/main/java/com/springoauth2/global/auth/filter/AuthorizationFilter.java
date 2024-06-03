package com.springoauth2.global.auth.filter;

import static com.springoauth2.global.util.Constant.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.springoauth2.api.application.auth.JwtProviderService;
import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.global.error.exception.NotFoundException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

	protected final JwtProviderService jwtProviderService;
	protected final HandlerExceptionResolver handlerExceptionResolver;

	public AuthorizationFilter(
		JwtProviderService jwtProviderService,
		@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {

		this.jwtProviderService = jwtProviderService;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Override
	protected void doFilterInternal(
		@NotNull HttpServletRequest httpServletRequest,
		@NotNull HttpServletResponse httpServletResponse,
		@NotNull FilterChain filterChain) {

		String accessToken = jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, httpServletRequest);
		String refreshToken = extractRefreshTokenFromCookies(httpServletRequest);

		try {
			if (jwtProviderService.isUsable(accessToken)) {
				authenticate(accessToken);
				filterChain.doFilter(httpServletRequest, httpServletResponse);

				return;
			}

			if (jwtProviderService.isUsable(refreshToken)) {
				accessToken = jwtProviderService.reGenerateToken(refreshToken, httpServletResponse);
				authenticate(accessToken);
				filterChain.doFilter(httpServletRequest, httpServletResponse);

				return;
			}

			throw new NotFoundException("❎[ERROR] JWT 토큰이 존재하지 않습니다.");
		} catch (Exception e) {
			log.warn("JWT ERROR 상세 설명: {}", e.getMessage());
			handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, e);
		}
	}

	protected void authenticate(String accessToken) {
		final AuthMember authMember = jwtProviderService.extractAuthMemberByAccessToken(accessToken);
		final Authentication authentication = new UsernamePasswordAuthenticationToken(authMember, BLANK);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private String extractRefreshTokenFromCookies(HttpServletRequest httpServletRequest) {
		if (httpServletRequest.getCookies() != null) {
			for (Cookie cookie : httpServletRequest.getCookies()) {
				if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
