package com.springoauth2.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
		// "/topic"으로 시작하는 메시지는 메시지 브로커가 관리
		messageBrokerRegistry.enableSimpleBroker("/topic");
		// 클라이언트가 "/app"으로 시작하는 메시지를 보낼 때, 이 메시지는 @MessageMapping으로 라우팅
		messageBrokerRegistry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		// 주소: "ws://localhost:8080/ws-stomp"
		// 웹 소켓 연결 엔드포인트 등록
		stompEndpointRegistry.addEndpoint("/ws").withSockJS();
	}
}
