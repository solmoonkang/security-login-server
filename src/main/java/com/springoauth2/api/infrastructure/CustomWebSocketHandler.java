package com.springoauth2.api.infrastructure;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.springoauth2.api.application.ChatService;
import com.springoauth2.api.domain.auth.AuthMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomWebSocketHandler extends TextWebSocketHandler {

	private final ChatService chatService;

	@Override
	public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
		Long chatRoomId = getChatRoomIdFromSession(webSocketSession);
		AuthMember authMember = getAuthMemberFromSession(webSocketSession);

		if (authMember != null) {
			chatService.addMemberToChatRoom(chatRoomId, authMember.nickname());
			webSocketSession.getAttributes().put("nickname", authMember.nickname());
			log.info("✅[SUCCESS] {} joined chat room {}", authMember.nickname(), chatRoomId);
		} else {
			log.warn("❎[ERROR] AuthMember not found in session attributes.");
			webSocketSession.close(CloseStatus.NOT_ACCEPTABLE.withReason("AuthMember not found."));
		}
	}

	private Long getChatRoomIdFromSession(WebSocketSession webSocketSession) {
		String chatRoomId = Objects.requireNonNull(webSocketSession.getUri()).getQuery().split("chatRoomId=")[1];
		return Long.parseLong(chatRoomId);
	}

	private AuthMember getAuthMemberFromSession(WebSocketSession webSocketSession) {
		return (AuthMember)webSocketSession.getAttributes().get("authMember");
	}
}
