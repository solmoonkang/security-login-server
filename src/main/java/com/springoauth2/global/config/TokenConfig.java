package com.springoauth2.global.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "jwt")
public class TokenConfig {

	private String iss;
	private String secretAccessKey;
	private long accessExpire;
	private long refreshExpire;
	private SecretKey secretKey;

	public TokenConfig(String iss, long accessExpire, long refreshExpire, String secretAccessKey) {
		this.iss = iss;
		this.accessExpire = accessExpire;
		this.refreshExpire = refreshExpire;
		this.secretAccessKey = secretAccessKey;
		initializeSecretKey();
	}

	@PostConstruct
	private void initializeSecretKey() {
		this.secretKey = Keys.hmacShaKeyFor(secretAccessKey.getBytes(StandardCharsets.UTF_8));
	}
}
