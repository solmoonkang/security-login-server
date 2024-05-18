package com.springoauth2.api.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.dto.CreateMemberRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;

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
