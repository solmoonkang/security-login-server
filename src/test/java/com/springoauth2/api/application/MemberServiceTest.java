package com.springoauth2.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.springoauth2.api.application.auth.JwtProviderService;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.dto.member.CreateMemberRequest;
import com.springoauth2.api.dto.member.LoginRequest;
import com.springoauth2.api.dto.member.LoginResponse;
import com.springoauth2.global.error.exception.BadRequestException;
import com.springoauth2.global.error.exception.ConflictException;
import com.springoauth2.global.error.exception.NotFoundException;
import com.springoauth2.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@InjectMocks
	MemberService memberService;

	@Mock
	JwtProviderService jwtProviderService;

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
	void signUp_userEmail_ConflictException_fail() {
		// GIVEN
		CreateMemberRequest createMemberRequest = MemberFixture.createMemberRequest();

		given(memberRepository.existsMemberByEmail(any(String.class))).willReturn(true);

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.signUp(createMemberRequest))
			.isInstanceOf(ConflictException.class)
			.hasMessage("❎[ERROR] 입력하신 이메일은 이미 존재하는 이메일입니다.");
	}

	@Test
	@DisplayName("SIGNUP(❌ CONFLICT): 해당 닉네임은 이미 존재하는 사용자 닉네임입니다.")
	void signUp_userNickname_ConflictException_fail() {
		// GIVEN
		CreateMemberRequest createMemberRequest = MemberFixture.createMemberRequest();

		given(memberRepository.existsMemberByNickname(any(String.class))).willReturn(true);

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.signUp(createMemberRequest))
			.isInstanceOf(ConflictException.class)
			.hasMessage("❎[ERROR] 입력하신 닉네임은 이미 존재하는 닉네임입니다.");
	}

	@Test
	@DisplayName("SIGNUP(❌ CONFLICT): 비밀번호와 확인 비밀번호가 동일하지 않습니다.")
	void signUp_passwordNotEqual_BadRequestException_fail() {
		// GIVEN
		CreateMemberRequest createMemberRequest = MemberFixture.createMemberRequestWithDifferentPassword();

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.signUp(createMemberRequest))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("❎[ERROR] 입력하신 비밀번호와 동일하지 않습니다.");
	}

	@Test
	@DisplayName("LOGIN(⭕️ SUCCESS): 사용자가 성공적으로 로그인을 완료했습니다.")
	void login_loginResponse_success() {
		// GIVEN
		String accessToken = "AccessToken";
		String refreshToken = "RefreshToken";
		Member member = MemberFixture.createMemberEntity();
		LoginRequest loginRequest = MemberFixture.createLoginRequest(member);

		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.of(member));
		given(passwordEncoder.matches(any(String.class), any(String.class))).willReturn(true);
		given(jwtProviderService.generateAccessToken(any(String.class), any(String.class))).willReturn(accessToken);
		given(jwtProviderService.generateRefreshToken(any(String.class))).willReturn(refreshToken);

		// WHEN
		LoginResponse actualLoginResponse = memberService.login(loginRequest, mockHttpServletResponse);

		// THEN
		assertThat(actualLoginResponse.accessToken()).isEqualTo(accessToken);
		assertThat(actualLoginResponse.refreshToken()).isEqualTo(refreshToken);
	}

	@Test
	@DisplayName("LOGIN(❌ FAILURE): 존재하지 않는 사용자 이메일로 로그인을 요청했습니다.")
	void login_email_NotFoundException_fail() {
		// GIVEN
		Member member = MemberFixture.createMemberEntity();
		LoginRequest loginRequest = MemberFixture.createLoginRequest(member);

		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.login(loginRequest, mockHttpServletResponse))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다.");
	}

	@Test
	@DisplayName("LOGIN(❌ FAILURE): 잘못된 사용자 비밀번호로 로그인을 요청했습니다.")
	void login_password_BadRequestException_fail() {
		// GIVEN
		Member member = MemberFixture.createMemberEntity();
		LoginRequest loginRequest = MemberFixture.createLoginRequest(member);

		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.of(member));
		given(passwordEncoder.matches(any(String.class), any(String.class))).willReturn(false);

		// WHEN & THEN
		assertThatThrownBy(() -> memberService.login(loginRequest, mockHttpServletResponse))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("❎[ERROR] 입력하신 비밀번호는 틀린 비밀번호입니다.");
	}
}