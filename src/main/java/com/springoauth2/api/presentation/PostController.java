package com.springoauth2.api.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springoauth2.api.application.PostService;
import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.dto.post.CreatePostRequest;
import com.springoauth2.api.dto.post.PostResponse;
import com.springoauth2.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<String> createPost(
		@Auth AuthMember authMember,
		@RequestBody @Validated CreatePostRequest createPostRequest
	) {
		postService.createPost(authMember, createPostRequest);
		return ResponseEntity.ok("SUCCESSFULLY CREATE POST");
	}

	@GetMapping
	public ResponseEntity<List<PostResponse>> getAllPosts(@Auth AuthMember authMember) {
		return ResponseEntity.ok(postService.getAllPosts(authMember));
	}
}
