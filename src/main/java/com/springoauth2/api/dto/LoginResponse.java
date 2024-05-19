package com.springoauth2.api.dto;

public record LoginResponse(
	String accessToken,
	String refreshToken
) {
}
