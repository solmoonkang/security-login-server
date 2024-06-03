package com.springoauth2.api.infrastructure;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.springoauth2.api.domain.auth.AuthMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final Map<String, String> sessionIdToNickname = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> chatRoomMembers = new ConcurrentHashMap<>();

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		log.info("[✅ LOGGER] HANDLE SUBSCRIBE EVENT CALLED");

		AuthMember authMember = extractAuthMemberFromAttributes(sessionSubscribeEvent);
		if (authMember == null) {
			authMember = new AuthMember("defaul@example.com", "defaultNickname");
		}
		log.info("[✅ LOGGER] MEMBER {} IS JOIN CHATROOM", authMember.nickname());

		addMemberToChatRoom(
			authMember,
			extractSessionIdFromHeaderAccessor(sessionSubscribeEvent),
			extractDestinationFromHeaderAccessor(sessionSubscribeEvent)
		);
	}

	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent sessionUnsubscribeEvent) {
		log.info("[✅ LOGGER] HANDLE UNSUBSCRIBE EVENT CALLED");

		AuthMember authMember = extractAuthMemberFromAttributes(sessionUnsubscribeEvent);
		if (authMember == null) {
			authMember = new AuthMember("default@example.com", "defaultNickname"); // 기본 값 사용
		}
		log.info("[✅ LOGGER] MEMBER {} IS LEFT CHATROOM", authMember.nickname());

		removeMemberBySessionId(
			extractSessionIdFromHeaderAccessor(sessionUnsubscribeEvent),
			extractDestinationFromHeaderAccessor(sessionUnsubscribeEvent)
		);
	}

	public void addMemberToChatRoom(AuthMember authMember, String sessionId, String destination) {
		sessionIdToNickname.put(sessionId, authMember.nickname());

		chatRoomMembers.computeIfAbsent(destination, member -> ConcurrentHashMap.newKeySet())
			.add(authMember.nickname());

		log.info("[✅ LOGGER] ADD MEMBERS TO CHATROOM: {} -> {}", destination, chatRoomMembers.get(destination));
	}

	public void removeMemberBySessionId(String sessionId, String destination) {
		String nickname = sessionIdToNickname.remove(sessionId);

		if (nickname == null) return;

		Set<String> members = chatRoomMembers.get(destination);
		if (members != null && members.remove(nickname) && members.isEmpty()) {
			chatRoomMembers.remove(destination);
		}

		log.info("[✅ LOGGER] REMOVE MEMBERS TO CHATROOM: {} -> {}", destination, chatRoomMembers.get(destination));
	}

	public Set<String> getActiveMembers(Long chatRoomId) {
		return chatRoomMembers.getOrDefault(chatRoomId.toString(), new HashSet<>());
	}

	private String extractSessionIdFromHeaderAccessor(AbstractSubProtocolEvent abstractSubProtocolEvent) {
		return SimpMessageHeaderAccessor.wrap(abstractSubProtocolEvent.getMessage()).getSessionId();
	}

	private String extractDestinationFromHeaderAccessor(AbstractSubProtocolEvent abstractSubProtocolEvent) {
		return SimpMessageHeaderAccessor.wrap(abstractSubProtocolEvent.getMessage()).getDestination();
	}

	private AuthMember extractAuthMemberFromAttributes(AbstractSubProtocolEvent abstractSubProtocolEvent) {
		SimpMessageHeaderAccessor simpMessageHeaderAccessor = SimpMessageHeaderAccessor
			.wrap(abstractSubProtocolEvent.getMessage());

		return (AuthMember)Objects.requireNonNull(simpMessageHeaderAccessor.getSessionAttributes()).get("authMember");
	}
}
