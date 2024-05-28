package com.springoauth2.api.domain.chat;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@Column(name = "sender_email")
	private String senderEmail;

	@Column(name = "sender_nickname")
	private String senderNickname;

	@Column(name = "message", columnDefinition = "TEXT")
	private String message;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime sendDate;

	@Builder
	private ChatMessage(ChatRoom chatRoom, String senderEmail, String senderNickname, String message) {
		this.chatRoom = chatRoom;
		this.senderEmail = senderEmail;
		this.senderNickname = senderNickname;
		this.message = message;
		this.sendDate = LocalDateTime.now();
	}
}
