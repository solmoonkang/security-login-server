package com.springoauth2.api.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreatePostRequest(
	@NotBlank(message = "게시글 제목을 입력해주세요.")
	String title,

	@NotBlank(message = "게시글 내용을 입력해주세요.")
	String content
) {
}
