package com.springoauth2.api.dto.chat;

public record MemberSessionResponse(
	String nickname,
	String sessionId,
	String destination
) {
}
