package com.springoauth2.support;

import com.springoauth2.api.dto.post.CreatePostRequest;

public class PostFixture {

	public static CreatePostRequest createPostRequest() {
		return CreatePostRequest.builder()
			.title("Test Post Title")
			.content("Test Post Content")
			.build();
	}
}
