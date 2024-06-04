package com.springoauth2.api.infrastructure;

import java.util.Objects;

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

	private final MemberSessionRegistry memberSessionRegistry;

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		log.info("[✅ LOGGER] HANDLE SUBSCRIBE EVENT CALLED");

		AuthMember authMember = extractAuthMemberFromAttributes(sessionSubscribeEvent);
		if (authMember == null) {
			authMember = new AuthMember("defaul@example.com", "defaultNickname");
		}
		log.info("[✅ LOGGER] MEMBER {} IS JOIN CHATROOM", authMember.nickname());

		final String sessionId = extractSessionIdFromHeaderAccessor(sessionSubscribeEvent);
		final String destination = extractDestinationFromHeaderAccessor(sessionSubscribeEvent);

		memberSessionRegistry.addSession(authMember.nickname(), sessionId, destination);
	}

	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent sessionUnsubscribeEvent) {
		log.info("[✅ LOGGER] HANDLE UNSUBSCRIBE EVENT CALLED");

		AuthMember authMember = extractAuthMemberFromAttributes(sessionUnsubscribeEvent);
		if (authMember == null) {
			authMember = new AuthMember("default@example.com", "defaultNickname");
		}
		log.info("[✅ LOGGER] MEMBER {} IS LEFT CHATROOM", authMember.nickname());

		final String sessionId = extractSessionIdFromHeaderAccessor(sessionUnsubscribeEvent);

		memberSessionRegistry.removeSession(sessionId);
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
