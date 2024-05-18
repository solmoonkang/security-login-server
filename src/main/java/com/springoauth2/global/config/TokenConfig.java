package com.springoauth2.global.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class TokenConfig {

	private final String iss;
	private final String salt;
	private final long accessExpire;
	private final long refreshExpire;
	private final SecretKey secretKey;

	public TokenConfig(String iss, long accessExpire, long refreshExpire, String salt) {
		this.iss = iss;
		this.accessExpire = accessExpire;
		this.refreshExpire = refreshExpire;
		this.salt = salt;
		this.secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
	}
}
