package com.springoauth2.api.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
	Long chatRoomId,
	String senderNickname,
	String content,
	LocalDateTime sendTime
) {

}
