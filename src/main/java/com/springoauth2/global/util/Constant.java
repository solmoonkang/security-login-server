package com.springoauth2.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constant {

	public static final String USER_EMAIL = "email";
	public static final String USER_NICKNAME = "nickname";

	public static final String BLANK = " ";
	public static final String BEARER = "Bearer";
	public static final String ACCESS_TOKEN_HEADER = "Authorization";
	public static final String REFRESH_TOKEN_COOKIE = "Authorization_RefreshToken";
}
