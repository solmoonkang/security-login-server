package com.springoauth2.api.dto.member;

public record LoginResponse(
	String accessToken,
	String refreshToken
) {
}
