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
		stompEndpointRegistry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*")
			.withSockJS();

		stompEndpointRegistry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*");
	}
}
