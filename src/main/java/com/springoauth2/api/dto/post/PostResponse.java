package com.springoauth2.api.dto.post;

public record PostResponse(
	String title,
	String content,
	String author
) {
}
