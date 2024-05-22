package com.springoauth2.api.application;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.domain.post.Post;
import com.springoauth2.api.domain.post.repository.PostRepository;
import com.springoauth2.api.dto.post.CreatePostRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final MemberRepository memberRepository;
	private final PostRepository postRepository;

	@Transactional
	public void createPost(AuthMember authMember, CreatePostRequest createPostRequest) {
		final Member member = memberRepository.findMemberByEmail(authMember.email())
			.orElseThrow(() -> new UsernameNotFoundException("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다."));

		final Post post = Post.builder()
			.title(createPostRequest.title())
			.content(createPostRequest.content())
			.member(member)
			.build();

		postRepository.save(post);
	}
}
