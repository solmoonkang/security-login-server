package com.springoauth2.api.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.springoauth2.api.application.ChatService;
import com.springoauth2.api.dto.chat.ChatMessageRequest;
import com.springoauth2.api.dto.chat.ChatMessageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@MessageMapping("/{chatRoomId}")
	@SendTo("/topic/{chatRoomId}")
	public ResponseEntity<ChatMessageResponse> sendChatMessage(@DestinationVariable Long chatRoomId, @Validated @RequestBody
	ChatMessageRequest chatMessageRequest) {
		return ResponseEntity.ok(chatService.createChatMessage(chatRoomId, chatMessageRequest));
	}
}
