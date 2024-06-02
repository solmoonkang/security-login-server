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

	/**
	 * WebSocketEventListener
	 * STOMP 프로토콜을 사용하여 웹소켓 통신을 처리하는 리스너로, 실시간 채팅 기능을 관리하는 데 사용된다.
	 * 이 클래스는 특히 채팅방의 구독 및 구독 해지 이벤트를 처리하고, 채팅방의 활성 사용자 목록을 유지 관리하는 역할을 한다.
	 */

	// 세션 ID와 사용자 닉네임을 매핑하는 ConcurrentHashMap. 웹소켓 세션과 사용자를 연결하는 데 사용
	private final Map<String, String> sessionIdToNickname = new ConcurrentHashMap<>();

	// 채팅방 ID와 해당 채팅방에 있는 사용자 닉네임 목록을 매핑하는 ConcurrentHashMap. 각 채팅방에 현재 접속해 있는 사용자 목록을 관리
	private final Map<String, Set<String>> chatRoomMembers = new ConcurrentHashMap<>();

	/**
	 * handleSubscribeEvent: 사용자가 특정 채팅방에 구독할 때 호출
	 *
	 */
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

	/**
	 * addMemberToChatRoom: 특정 사용자를 지정된 채팅방에 추가
	 * chatRoomMembers 맵에 주어진 닉네임을 추가하여 지정된 채팅방의 사용자 목록을 갱신
	 * 특정 상황에서 프로그래밍적으로 사용자를 채팅방에 추가하는 기능을 제공
	 */
	public void addMemberToChatRoom(Long chatRoomId, String nickname) {
		chatRoomMembers.computeIfAbsent(chatRoomId.toString(), member -> new HashSet<>()).add(nickname);
	}

	/**
	 * getActiveMembers: 지정된 채팅방의 현재 활성 사용자 목록을 반환
	 * 주어진 채팅방 ID에 대해 활성 사용자 목록을 조회
	 * 현재 채팅방에 접속해 있는 사용자 목록을 다른 부분에서 쉽게 조회
	 */
	public Set<String> getActiveMembers(Long chatRoomId) {
		return chatRoomMembers.getOrDefault(chatRoomId.toString(), new HashSet<>());
	}
}
