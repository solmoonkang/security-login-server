package com.springoauth2.api.infrastructure;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import com.springoauth2.global.error.exception.NotFoundException;

@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {

	private static final byte[] EMPTY_PAYLOAD = new byte[0];

	public StompExceptionHandler() {
		super();
	}

	@Override
	public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable throwable) {
		final Throwable exception = converterTrowException(throwable);

		if (exception instanceof NotFoundException) {
			return handleNotFoundException(clientMessage, exception);
		}

		return super.handleClientMessageProcessingError(clientMessage, throwable);
	}

	private Throwable converterTrowException(Throwable exception) {
		if (exception instanceof MessageDeliveryException) {
			return exception.getCause();
		}

		return exception;
	}

	private Message<byte[]> handleNotFoundException(Message<byte[]> clientMessage, Throwable exception) {
		return prepareErrorMessage(clientMessage, exception.getMessage(), HttpStatus.UNAUTHORIZED.name());
	}

	private Message<byte[]> prepareErrorMessage(Message<byte[]> clientMessage, String message, String errorCode) {
		final StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
		stompHeaderAccessor.setMessage(errorCode);
		stompHeaderAccessor.setLeaveMutable(true);

		setReceiptIdForClient(clientMessage, stompHeaderAccessor);

		return MessageBuilder.createMessage(
			message != null ? message.getBytes(StandardCharsets.UTF_8) : EMPTY_PAYLOAD,
			stompHeaderAccessor.getMessageHeaders()
		);
	}

	private void setReceiptIdForClient(Message<byte[]> clientMessage, StompHeaderAccessor stompHeaderAccessor) {
		if (Objects.isNull(clientMessage)) {
			return;
		}

		final StompHeaderAccessor clientHeaderAccessor = MessageHeaderAccessor.getAccessor(
			clientMessage, StompHeaderAccessor.class);
		final String receiptId = Objects.isNull(clientHeaderAccessor) ? null : clientHeaderAccessor.getReceipt();

		if (receiptId != null) {
			stompHeaderAccessor.setReceiptId(receiptId);
		}
	}

	@Override
	protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor, byte[] errorPayload,
		Throwable cause, StompHeaderAccessor clientHeaderAccessor) {
		return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());
	}
}
