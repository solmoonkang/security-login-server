package com.springoauth2.api.domain.chat;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.dto.chat.ChatMessageRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_message_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Column(name = "message", columnDefinition = "TEXT")
	private String message;

	@CreatedDate
	@Column(name = "send_at", updatable = false)
	private LocalDateTime sendAt;

	@Column(name = "is_blined")
	private boolean isBlinded = false;

	@Builder
	private ChatMessage(ChatRoom chatRoom, Member member, String message) {
		this.chatRoom = chatRoom;
		this.member = member;
		this.message = message;
		this.sendAt = LocalDateTime.now();
	}

	public static ChatMessage createChatMessage(ChatRoom chatRoom, Member member,
		ChatMessageRequest chatMessageRequest) {
		return ChatMessage.builder()
			.chatRoom(chatRoom)
			.member(member)
			.message(chatMessageRequest.message())
			.build();
	}
}
