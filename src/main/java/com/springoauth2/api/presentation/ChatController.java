package com.springoauth2.api.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springoauth2.api.application.ChatService;
import com.springoauth2.api.dto.chat.ChatMessageRequest;
import com.springoauth2.api.dto.chat.ChatMessageResponse;
import com.springoauth2.api.dto.chat.ChatRoomRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final SimpMessageSendingOperations simpMessageSendingOperations;

	@PostMapping("/api/chat-room")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<String> createChatRoom(@Validated @RequestBody ChatRoomRequest chatRoomRequest) {
		chatService.createChatRoom(chatRoomRequest);
		return ResponseEntity.ok("OK");
	}

	@MessageMapping("/ws/{chatRoomId}/chat-message")
	public void sendChatMessage(@DestinationVariable Long chatRoomId,
		@Validated @RequestBody ChatMessageRequest chatMessageRequest) {
		chatService.saveChatMessage(chatRoomId, chatMessageRequest);
		simpMessageSendingOperations.convertAndSend("/topic/ws/" + chatRoomId, chatMessageRequest.message());
	}

	@GetMapping("/api/{chatRoomId}/chat-message")
	public ResponseEntity<List<ChatMessageResponse>> getChatMessageList(@PathVariable Long chatRoomId) {
		return ResponseEntity.ok(chatService.getChatMessageList(chatRoomId));
	}
}
