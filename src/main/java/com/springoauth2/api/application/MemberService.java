package com.springoauth2.api.application;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springoauth2.api.application.auth.JwtProviderService;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.dto.member.CreateMemberRequest;
import com.springoauth2.api.dto.member.LoginRequest;
import com.springoauth2.api.dto.member.LoginResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final PasswordEncoder passwordEncoder;

	private final MemberRepository memberRepository;
	private final JwtProviderService jwtProviderService;

	@Transactional
	public void signUp(CreateMemberRequest createMemberRequest) {
		validateEmailNotExists(createMemberRequest.email());
		validateNicknameNotExists(createMemberRequest.nickname());
		validatePasswordEquality(createMemberRequest.password(), createMemberRequest.checkPassword());

		final Member member = Member.createMember(
			createMemberRequest,
			passwordEncoder.encode(createMemberRequest.password())
		);

		memberRepository.save(member);
	}

	@Transactional
	public LoginResponse login(LoginRequest loginRequest) {
		final Member member = memberRepository.findMemberByEmail(loginRequest.email())
			.orElseThrow(() -> new UsernameNotFoundException("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다."));
		validatePasswordMatched(loginRequest.password(), member.getPassword());

		final String accessToken = jwtProviderService.generateAccessToken(member.getEmail(), member.getNickname());
		final String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		member.updateRefreshToken(refreshToken);

		return new LoginResponse(accessToken, refreshToken);
	}

	private void validatePasswordMatched(String rawPassword, String encodedPassword) {
		if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new IllegalArgumentException("❎[ERROR] 입력하신 비밀번호는 틀린 비밀번호입니다.");
		}
	}

	private void validateEmailNotExists(String email) {
		if (memberRepository.existsMemberByEmail(email)) {
			throw new IllegalArgumentException("❎[ERROR] 입력하신 이메일은 이미 존재하는 이메일입니다.");
		}
	}

	private void validateNicknameNotExists(String nickname) {
		if (memberRepository.existsMemberByNickname(nickname)) {
			throw new IllegalArgumentException("❎[ERROR] 입력하신 닉네임은 이미 존재하는 닉네임입니다.");
		}
	}

	private void validatePasswordEquality(String password, String checkPassword) {
		if (!password.equals(checkPassword)) {
			throw new IllegalArgumentException("❎[ERROR] 입력하신 비밀번호와 동일하지 않습니다.");
		}
	}
}
