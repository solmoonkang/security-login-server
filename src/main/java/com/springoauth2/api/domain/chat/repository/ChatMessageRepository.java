package com.springoauth2.api.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springoauth2.api.domain.chat.ChatMessage;
import com.springoauth2.api.domain.chat.ChatRoom;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findChatMessageByChatRoom(ChatRoom chatRoom);
}
