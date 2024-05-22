package com.springoauth2.support;

import java.util.Collections;
import java.util.List;

import com.springoauth2.api.domain.post.Post;
import com.springoauth2.api.dto.post.CreatePostRequest;

public class PostFixture {

	public static CreatePostRequest createPostRequest() {
		return CreatePostRequest.builder()
			.title("Test Post Title")
			.content("Test Post Content")
			.build();
	}

	public static List<Post> createPostList() {
		return Collections.singletonList(createPostEntity());
	}

	public static Post createPostEntity() {
		return Post.createPost(MemberFixture.createMemberEntity(), createPostRequest());
	}
}
