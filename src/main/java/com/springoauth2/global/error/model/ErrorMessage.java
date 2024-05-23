package com.springoauth2.global.error.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorMessage {

	FAILED_UNKNOWN_SZS("서버에서 알 수 없는 에러가 발생했습니다."),

	FAILED_AES_128("AES 128 암/복호화 생성을 실패했습니다."),
	FAILED_AES_ENCRYPT("AES 128 암호화가 실패했습니다."),
	FAILED_AES_DECRYPT("AES 128 암호화가 실패했습니다."),

	EXPIRED_JWT("만료된 JWT 토큰입니다."),

	INVALID_REQUEST_FIELD("형식에 맞지 않는 요청 정보입니다."),
	INVALID_PASSWORD("비밀번호가 올바르지 않습니다."),
	INVALID_JWT("잘못된 JWT 토큰입니다."),

	CONFLICT_USER_ID("이미 존재하는 사용자 아이디입니다."),
	CONFLICT_REG_NO("이미 존재하는 사용자입니다."),

	NOT_FOUND_USER_ID("해당 사용자 아이디는 존재하지 않는 사용자입니다."),
	NOT_FOUND_ROLE("존재하지 않는 권한입니다."),
	NOT_FOUND_JWT("JWT 토큰을 찾을 수 없습니다.");

	private final String message;
}
