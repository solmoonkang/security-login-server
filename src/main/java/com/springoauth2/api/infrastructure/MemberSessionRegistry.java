package com.springoauth2.api.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.springoauth2.api.dto.chat.MemberSessionResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MemberSessionRegistry {

	private final Map<String, MemberSessionResponse> memberSessionRegistry = new ConcurrentHashMap<>();

	public void addSession(String nickname, String sessionId, String destination) {
		MemberSessionResponse memberSessionResponse = new MemberSessionResponse(nickname, sessionId, destination);
		memberSessionRegistry.put(sessionId, memberSessionResponse);

		log.info("[✅ LOGGER] SESSION ADDED: {}", memberSessionResponse);
	}

	public void removeSession(String sessionId) {
		memberSessionRegistry.remove(sessionId);

		log.info("[✅ LOGGER] SESSION REMOVED: SessionId={}", sessionId);
	}

	public List<String> getMembersInChatRoom(String destination) {
		List<String> members = memberSessionRegistry.values().stream()
			.filter(session -> session.destination().equals(destination))
			.map(MemberSessionResponse::nickname)
			.toList();

		log.info("[✅ LOGGER] MEMBERS IN {}: {}", destination, members);

		return members;
	}
}
