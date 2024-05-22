package com.springoauth2.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
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
import com.springoauth2.api.dto.post.PostResponse;
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
	@DisplayName("CREATE POST(❌ FAILURE): 사용자를 찾을 수 없어 게시글 생성에 실패했습니다.")
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

	@Test
	@DisplayName("GET ALL POSTS(⭕️ SUCCESS): 사용자가 성공적으로 게시글 목록 조회를 완료했습니다.")
	void getAllPost_postResponse_success() {
		// GIVEN
		AuthMember authMember = MemberFixture.createAuthMember();
		Member member = MemberFixture.createMemberEntity();
		List<Post> posts = PostFixture.createPostList();

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.of(member));
		given(postRepository.findPostByMember(member)).willReturn(posts);

		// WHEN
		List<PostResponse> actualPostResponses = postService.getAllPosts(authMember);

		// THEN
		assertThat(actualPostResponses).hasSize(posts.size());

		List<PostResponse> postResponses = posts.stream()
			.map(post -> new PostResponse(post.getTitle(), post.getContent(), member.getNickname()))
			.toList();

		assertThat(actualPostResponses)
			.usingRecursiveFieldByFieldElementComparator()
			.containsExactlyElementsOf(postResponses);

		verify(memberRepository).findMemberByEmail(any(String.class));
		verify(postRepository).findPostByMember(member);
	}

	@Test
	@DisplayName("GET ALL POSTS(❌ FAILURE): 사용자를 찾을 수 없어 게시글 목록 조회에 실패했습니다.")
	void getAllPost_UsernameNotFoundException_fail() {
		// GIVEN
		AuthMember authMember = MemberFixture.createAuthMember();

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> postService.getAllPosts(authMember))
			.isInstanceOf(UsernameNotFoundException.class)
			.hasMessage("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다.");

		verify(memberRepository).findMemberByEmail(any(String.class));
		verify(postRepository, never()).findPostByMember(any(Member.class));
	}
}