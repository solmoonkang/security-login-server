package com.springoauth2.api.domain.auth;

import java.util.Objects;

public record AuthMember(
	String email,
	String nickname
) {

	public static AuthMember createAuthMember(String email, String nickname) {
		return new AuthMember(
			Objects.requireNonNull(email),
			Objects.requireNonNull(nickname)
		);
	}
}
