package com.springoauth2.global.auth.filter;

import static com.springoauth2.global.util.Constant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.springoauth2.api.application.auth.JwtProviderService;
import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.support.MemberFixture;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthorizationFilterTest {

	@InjectMocks
	AuthorizationFilter authorizationFilter;

	@Mock
	JwtProviderService jwtProviderService;

	@Mock
	HandlerExceptionResolver handlerExceptionResolver;

	MockHttpServletRequest mockHttpServletRequest;
	MockHttpServletResponse mockHttpServletResponse;
	MockFilterChain mockFilterChain;

	@BeforeEach
	void setUp() {
		authorizationFilter = new AuthorizationFilter(jwtProviderService, handlerExceptionResolver);

		mockHttpServletRequest = new MockHttpServletRequest();
		mockHttpServletResponse = new MockHttpServletResponse();
		mockFilterChain = new MockFilterChain();
	}

	@Test
	@DisplayName("DO FILTER INTERNAL(⭕️ SUCCESS): AccessToken이 유효한 토큰으로 인증/인가 필터가 성공적으로 동작했습니다.")
	void doFilterInternal_accessToken_void_success() {
		// GIVEN
		String accessToken = "accessToken";
		AuthMember authMember = MemberFixture.createAuthMember();

		given(jwtProviderService.extractToken(
			eq(ACCESS_TOKEN_HEADER),
			any(HttpServletRequest.class))
		).willReturn(accessToken);
		given(jwtProviderService.isUsable(accessToken)).willReturn(true);
		given(jwtProviderService.extractAuthMemberByAccessToken(accessToken)).willReturn(authMember);

		// WHEN
		authorizationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
		Authentication actualAuthentication = SecurityContextHolder.getContext().getAuthentication();

		// THEN
		verify(jwtProviderService, times(1)).isUsable(accessToken);
		verify(jwtProviderService, times(1)).extractAuthMemberByAccessToken(accessToken);

		assertThat(actualAuthentication.getPrincipal()).isEqualTo(authMember);
	}

	@Test
	@DisplayName("DO FILTER INTERNAL(❌ FAIL): Access/RefreshToken이 만료된 토큰으로 인증/인가 필터를 통과하지 못했습니다.")
	void doFilterInternal_expired_IllegalArgumentException_fail() {
		// GIVEN
		String token = "Access-RefreshToken";

		given(jwtProviderService.extractToken(any(String.class), any(HttpServletRequest.class))).willReturn(token);
		given(jwtProviderService.isUsable(any(String.class))).willReturn(false);

		// WHEN
		authorizationFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

		// THEN
		verify(handlerExceptionResolver)
			.resolveException(
				eq(mockHttpServletRequest),
				eq(mockHttpServletResponse),
				isNull(),
				any(IllegalArgumentException.class)
			);
	}
}