package com.springoauth2.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.springoauth2.api.application.auth.JwtProviderService;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;

@TestConfiguration
public class TokenConfigTest {

	@Bean
	@Primary
	public TokenConfig tokenConfig() {
		return new TokenConfig("test", 60000, 86400000, "test_test_test_test_test_test_test_test_test_test_test");
	}

	@Bean
	@Primary
	public JwtProviderService jwtProviderService(TokenConfig tokenConfig, MemberRepository memberRepository) {
		return new JwtProviderService(tokenConfig, memberRepository);
	}
}