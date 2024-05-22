package com.springoauth2.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.domain.post.Post;
import com.springoauth2.api.domain.post.repository.PostRepository;
import com.springoauth2.api.dto.post.CreatePostRequest;
import com.springoauth2.support.MemberFixture;
import com.springoauth2.support.PostFixture;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	@InjectMocks
	PostService postService;

	@Mock
	PostRepository postRepository;

	@Mock
	MemberRepository memberRepository;

	@Test
	@DisplayName("CREATE POST(⭕️ SUCCESS): 사용자가 성공적으로 게시글 생성을 완료했습니다.")
	void createPost_void_success() {
		// GIVEN
		AuthMember authMember = MemberFixture.createAuthMember();
		CreatePostRequest createPostRequest = PostFixture.createPostRequest();
		Member member = MemberFixture.createMemberEntity();

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.of(member));

		// WHEN
		postService.createPost(authMember, createPostRequest);

		// THEN
		verify(postRepository).save(any(Post.class));
	}

	@Test
	@DisplayName("CREATE POST(❌ FAIL): 존재하지 않는 사용자가 게시글 생성을 시도하여 실패했습니다.")
	void createPost_UsernameNotFoundException_fail() {
		// GIVEN
		AuthMember authMember = MemberFixture.createAuthMember();
		CreatePostRequest createPostRequest = PostFixture.createPostRequest();

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> postService.createPost(authMember, createPostRequest))
			.isInstanceOf(UsernameNotFoundException.class)
			.hasMessage("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다.");
	}
}