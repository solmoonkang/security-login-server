package com.springoauth2.api.application;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.domain.chat.ChatMessage;
import com.springoauth2.api.domain.chat.ChatRoom;
import com.springoauth2.api.domain.chat.repository.ChatMessageRepository;
import com.springoauth2.api.domain.chat.repository.ChatRoomRepository;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.dto.chat.ChatMessageRequest;
import com.springoauth2.api.dto.chat.ChatMessageResponse;
import com.springoauth2.api.dto.chat.ChatRoomRequest;
import com.springoauth2.api.dto.member.MemberResponse;
import com.springoauth2.api.infrastructure.WebSocketEventListener;
import com.springoauth2.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

	/**
	 * 불필요한 서비스 로직 및 핸들러, 인터셉터 정리
	 * 각 메서드가 무엇을 의미하는지를 파악하고 해당 메서드의 역할을 주석으로 표시
	 */

	private final Map<Long, Set<String>> chatRoomMembers = new ConcurrentHashMap<>();

	private final MemberRepository memberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final WebSocketEventListener webSocketEventListener;

	public void createChatRoom(ChatRoomRequest chatRoomRequest) {
		final ChatRoom chatRoom = ChatRoom.createChatRoom(chatRoomRequest);
		chatRoomRepository.save(chatRoom);
	}

	public void addMemberToChatRoom(Long chatRoomId, String nickname) {
		chatRoomMembers.computeIfAbsent(chatRoomId, member -> new HashSet<>()).add(nickname);
	}

	public Set<String> getMembersInChatRoom(Long chatRoomId) {
		return chatRoomMembers.getOrDefault(chatRoomId, Collections.emptySet());
	}

	@Transactional
	public void saveAndSendChatMessage(Long chatRoomId, AuthMember authMember, ChatMessageRequest chatMessageRequest) {
		if (authMember == null) {
			throw new AccessDeniedException("Unauthorized access to chat room");
		}

		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final Member member = getMemberByEmail(authMember.email());

		final ChatMessage chatMessage = ChatMessage.createChatMessage(chatRoom, member, chatMessageRequest);
		chatMessageRepository.save(chatMessage);

		webSocketEventListener.addMemberToChatRoom(chatRoomId, member.getNickname());
	}

	public List<ChatMessageResponse> getChatMessageList(Long chatRoomId) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessageByChatRoom(chatRoom);

		return chatMessageList.stream()
			.map(this::convertToChatMessageResponse)
			.toList();
	}

	/**
	 * 현재 컨트롤러에 WebSocketEventListener에 구현된 채팅방 사용자 조회 로직을 서비스 레이어에서 사용할 수 있도록 구현
	 */
	public List<MemberResponse> getLoggedInVisitors(Long chatRoomId) {
		Set<String> memberNicknames = webSocketEventListener.getActiveMembers(chatRoomId);
		List<Member> memberList = memberRepository.findAllByNicknameIn(memberNicknames);

		return memberList.stream()
			.map(member -> new MemberResponse(member.getNickname()))
			.toList();
	}

	private ChatRoom getChatRoomById(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new NotFoundException("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다."));
	}

	private Member getMemberByEmail(String email) {
		return memberRepository.findMemberByEmail(email)
			.orElseThrow(() -> new NotFoundException("[❎ ERROR] 요청하신 회원을 찾을 수 없습니다."));
	}

	private ChatMessageResponse convertToChatMessageResponse(ChatMessage chatMessage) {
		return new ChatMessageResponse(
			chatMessage.getChatRoom().getId(),
			chatMessage.getSender(),
			chatMessage.getMessage(),
			chatMessage.getSendAt());
	}
}
