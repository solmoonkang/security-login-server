package com.springoauth2.api.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
	@NotBlank(message = "[❎ ERROR] 사용자 이메일을 입력해주세요.")
	String memberEmail,

	@NotBlank(message = "[❎ ERROR] 메시지 내용을 입력해주세요.")
	@Size(max = 50)
	String content
) {
}