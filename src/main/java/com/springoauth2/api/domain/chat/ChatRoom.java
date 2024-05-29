package com.springoauth2.api.domain.chat;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.springoauth2.api.domain.gallery.Gallery;

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
@Table(name = "tbl_chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_room_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private Gallery gallery;

	@Column(name = "name")
	private String name;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Builder
	private ChatRoom(Gallery gallery, String name) {
		this.gallery = gallery;
		this.name = name;
		this.createdAt = LocalDateTime.now();
	}

	public static ChatRoom createChatRoom(Gallery gallery, String name) {
		return ChatRoom.builder()
			.gallery(gallery)
			.name(name)
			.build();
	}
}
