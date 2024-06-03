package com.springoauth2.api.presentation;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springoauth2.api.application.ChatService;
import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.dto.chat.ChatMessageRequest;
import com.springoauth2.api.dto.chat.ChatMessageResponse;
import com.springoauth2.api.dto.chat.ChatRoomRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final SimpMessageSendingOperations simpMessageSendingOperations;

	@PostMapping("/api/chat-rooms")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<String> enterChatRoom(@Validated @RequestBody ChatRoomRequest chatRoomRequest) {
		chatService.createChatRoom(chatRoomRequest);
		return ResponseEntity.ok("OK");
	}

	@MessageMapping(value = "/ws/{chatRoomId}/chat-messages")
	public void saveAndSendChatMessage(@DestinationVariable Long chatRoomId,
		@Payload ChatMessageRequest chatMessageRequest,
		SimpMessageHeaderAccessor simpMessageHeaderAccessor
	) {
		AuthMember authMember = (AuthMember)simpMessageHeaderAccessor.getSessionAttributes().get("authMember");
		chatService.saveAndSendChatMessage(chatRoomId, authMember, chatMessageRequest);
		simpMessageSendingOperations.convertAndSend("/sub/ws/" + chatRoomId, chatMessageRequest.message());
	}

	@GetMapping("/api/chat-rooms/{chatRoomId}/members")
	public ResponseEntity<Set<String>> getLoggedInVisitors(@PathVariable Long chatRoomId) {
		return ResponseEntity.ok(chatService.getLoggedInVisitors(chatRoomId));
	}

	@GetMapping("/api/{chatRoomId}/chat-messages")
	public ResponseEntity<List<ChatMessageResponse>> getChatMessageList(@PathVariable Long chatRoomId) {
		return ResponseEntity.ok(chatService.getChatMessageList(chatRoomId));
	}
}
