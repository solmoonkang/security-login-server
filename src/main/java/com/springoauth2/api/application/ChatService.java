package com.springoauth2.api.application;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	private final MemberRepository memberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final WebSocketEventListener webSocketEventListener;

	public void createChatRoom(ChatRoomRequest chatRoomRequest) {
		final ChatRoom chatRoom = ChatRoom.createChatRoom(chatRoomRequest);
		chatRoomRepository.save(chatRoom);
	}

	@Transactional
	public void saveAndSendChatMessage(Long chatRoomId, ChatMessageRequest chatMessageRequest) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final Member member = getMemberByEmail(chatMessageRequest.email());

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
