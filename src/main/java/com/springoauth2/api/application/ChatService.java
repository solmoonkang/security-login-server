package com.springoauth2.api.application;

import java.util.List;

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
import com.springoauth2.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

	private final MemberRepository memberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public void createChatRoom(ChatRoomRequest chatRoomRequest) {
		final ChatRoom chatRoom = ChatRoom.createChatRoom(chatRoomRequest);
		chatRoomRepository.save(chatRoom);
	}

	@Transactional
	public void saveChatMessage(Long chatRoomId, ChatMessageRequest chatMessageRequest) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final Member member = getMemberByEmail(chatMessageRequest.email());

		final ChatMessage chatMessage = ChatMessage.createChatMessage(chatRoom, member, chatMessageRequest);
		chatMessageRepository.save(chatMessage);
	}

	public List<ChatMessageResponse> getChatMessageList(Long chatRoomId) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessageByChatRoom(chatRoom);

		return chatMessageList.stream()
			.map(this::convertToChatMessageResponse)
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
			chatMessage.getMember().getEmail(),
			chatMessage.getMember().getNickname(),
			chatMessage.getMessage(),
			chatMessage.getSendAt());
	}
}
