package com.springoauth2.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.dto.CreateMemberRequest;
import com.springoauth2.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@InjectMocks
	MemberService memberService;

	@Mock
	MemberRepository memberRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("SIGNUP(⭕️ SUCCESS): 사용자가 성공적으로 회원가입을 완료했습니다.")
	void signUp_void_success() {
		// GIVEN
		CreateMemberRequest createMemberRequest = MemberFixture.createMemberRequest();

		given(memberRepository.existsMemberByEmail(any(String.class))).willReturn(false);
		given(memberRepository.existsMemberByNickname(any(String.class))).willReturn(false);
		given(passwordEncoder.encode(any(String.class))).willReturn("encodedPassword");

		// WHEN
		memberService.signUp(createMemberRequest);

		// THEN
		verify(memberRepository).save(any(Member.class));
	}

	@Test
	@DisplayName("SIGNUP(❌ CONFLICT): 해당 이메일은 이미 존재하는 사용자 이메일입니다.")
	void signUp_userEmail_conflictException_fail() {
		// GIVEN
		CreateMemberRequest createMemberRequest = MemberFixture.createMemberRequest();

		given(memberRepository.existsMemberByEmail(any(String.class))).willReturn(true);

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.signUp(createMemberRequest))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("❎[ERROR] 입력하신 이메일은 이미 존재하는 이메일입니다.");
	}

	@Test
	@DisplayName("SIGNUP(❌ CONFLICT): 해당 닉네임은 이미 존재하는 사용자 닉네임입니다.")
	void signUp_userNickname_conflictException_fail() {
		// GIVEN
		CreateMemberRequest createMemberRequest = MemberFixture.createMemberRequest();

		given(memberRepository.existsMemberByNickname(any(String.class))).willReturn(true);

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.signUp(createMemberRequest))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("❎[ERROR] 입력하신 닉네임은 이미 존재하는 닉네임입니다.");
	}

	@Test
	@DisplayName("SIGNUP(❌ CONFLICT): 비밀번호와 확인 비밀번호가 동일하지 않습니다.")
	void signUp_passwordNotEqual_conflictException_fail() {
		// GIVEN
		CreateMemberRequest createMemberRequest = MemberFixture.createMemberRequestWithDifferentPassword();

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.signUp(createMemberRequest))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("❎[ERROR] 입력하신 비밀번호와 동일하지 않습니다.");
	}
}