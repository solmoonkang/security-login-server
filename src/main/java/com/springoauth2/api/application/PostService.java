package com.springoauth2.api.application;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.domain.post.Post;
import com.springoauth2.api.domain.post.repository.PostRepository;
import com.springoauth2.api.dto.post.CreatePostRequest;
import com.springoauth2.api.dto.post.PostResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final MemberRepository memberRepository;
	private final PostRepository postRepository;

	@Transactional
	public void createPost(AuthMember authMember, CreatePostRequest createPostRequest) {
		final Member member = getMemberByEmail(authMember.email());

		final Post post = Post.createPost(member, createPostRequest);

		postRepository.save(post);
	}

	public List<PostResponse> getAllPosts(AuthMember authMember) {
		final Member member = getMemberByEmail(authMember.email());
		final List<Post> posts = postRepository.findPostByMember(member);

		return posts.stream()
			.map(post -> convertToPostResponse(post, member))
			.toList();
	}

	private PostResponse convertToPostResponse(Post post, Member member) {
		return new PostResponse(post.getTitle(), post.getContent(), member.getNickname());
	}

	private Member getMemberByEmail(String email) {
		return memberRepository.findMemberByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다."));
	}
}
