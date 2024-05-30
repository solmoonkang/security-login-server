package com.springoauth2.api.domain.chat;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.springoauth2.api.dto.chat.ChatRoomRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_room_id")
	private Long id;

	@Column(name = "name")
	private String name;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Builder
	private ChatRoom(String name) {
		this.name = name;
		this.createdAt = LocalDateTime.now();
	}

	public static ChatRoom createChatRoom(ChatRoomRequest chatRoomRequest) {
		return ChatRoom.builder()
			.name(chatRoomRequest.title())
			.build();
	}
}
