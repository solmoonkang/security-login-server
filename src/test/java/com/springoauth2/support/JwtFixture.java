package com.springoauth2.support;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtFixture {

	public static String createExpiredToken(SecretKey secretKey) {
		return Jwts.builder()
			.expiration(new Date(System.currentTimeMillis() - 3600 * 1000))
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}
}
