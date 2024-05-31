package com.springoauth2.api.infrastructure;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.springoauth2.api.domain.auth.AuthMember;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketEventListener {

	private final Map<String, String> sessionIdToNickname = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> chatRoomMembers = new ConcurrentHashMap<>();

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		String sessionId = Objects.requireNonNull(
			sessionSubscribeEvent.getMessage().getHeaders().get("simpSessionId")).toString();

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(sessionSubscribeEvent.getMessage());
		String destination = stompHeaderAccessor.getDestination();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()) {
			String nickname = ((AuthMember)authentication.getPrincipal()).nickname();
			sessionIdToNickname.put(sessionId, nickname);

			if (Objects.requireNonNull(destination).startsWith("/sub/ws")) {
				String chatRoomId = destination.split("/")[3];
				chatRoomMembers.computeIfAbsent(chatRoomId, member -> new HashSet<>()).add(nickname);
				log.info("Member subscribed to chat room, chatRoomId: {}, nickname: {}", chatRoomId, nickname);
			} else {
				log.warn("Unauthorized access attempt to chat room: {}", destination);
			}
		} else {
			log.warn("Unauthenticated access attempt to chat room: {}", destination);
		}
	}

	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent sessionUnsubscribeEvent) {
		String sessionId = Objects.requireNonNull(
			sessionUnsubscribeEvent.getMessage().getHeaders().get("simpSessionId")).toString();

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(sessionUnsubscribeEvent.getMessage());
		String destination = stompHeaderAccessor.getDestination();
		String nickname = sessionIdToNickname.get(sessionId);

		if (Objects.requireNonNull(destination).startsWith("/sub/ws")) {
			String chatRoomId = destination.split("/")[3];
			chatRoomMembers.getOrDefault(chatRoomId, new HashSet<>()).remove(nickname);
			log.info("Member unsubscribed from chat room, chatRoomId: {}, nickname: {}", chatRoomId, nickname);
		}
	}

	public void addMemberToChatRoom(Long chatRoomId, String nickname) {
		chatRoomMembers.computeIfAbsent(chatRoomId.toString(), member -> new HashSet<>()).add(nickname);
	}

	public Set<String> getActiveMembers(Long chatRoomId) {
		return chatRoomMembers.getOrDefault(chatRoomId.toString(), new HashSet<>());
	}
}
