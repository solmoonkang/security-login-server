package com.springoauth2.api.application;

import static com.springoauth2.global.util.Constant.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springoauth2.api.application.auth.JwtProviderService;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.api.dto.member.CreateMemberRequest;
import com.springoauth2.api.dto.member.LoginRequest;
import com.springoauth2.api.dto.member.LoginResponse;
import com.springoauth2.global.error.exception.BadRequestException;
import com.springoauth2.global.error.exception.ConflictException;
import com.springoauth2.global.error.exception.NotFoundException;
import com.springoauth2.global.util.CookieUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
	public LoginResponse login(LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
		final Member member = memberRepository.findMemberByEmail(loginRequest.email())
			.orElseThrow(() -> new NotFoundException("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다."));
		validatePasswordMatched(loginRequest.password(), member.getPassword());

		final String accessToken = jwtProviderService.generateAccessToken(member.getEmail(), member.getNickname());
		final String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		member.updateRefreshToken(refreshToken);

		addRefreshTokenCookie(refreshToken, httpServletResponse);

		return new LoginResponse(accessToken, refreshToken);
	}

	private void validatePasswordMatched(String rawPassword, String encodedPassword) {
		if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new BadRequestException("❎[ERROR] 입력하신 비밀번호는 틀린 비밀번호입니다.");
		}
	}

	private void validateEmailNotExists(String email) {
		if (memberRepository.existsMemberByEmail(email)) {
			throw new ConflictException("❎[ERROR] 입력하신 이메일은 이미 존재하는 이메일입니다.");
		}
	}

	private void validateNicknameNotExists(String nickname) {
		if (memberRepository.existsMemberByNickname(nickname)) {
			throw new ConflictException("❎[ERROR] 입력하신 닉네임은 이미 존재하는 닉네임입니다.");
		}
	}

	private void validatePasswordEquality(String password, String checkPassword) {
		if (!password.equals(checkPassword)) {
			throw new BadRequestException("❎[ERROR] 입력하신 비밀번호와 동일하지 않습니다.");
		}
	}

	private void addRefreshTokenCookie(String refreshToken, HttpServletResponse httpServletResponse) {
		Cookie refreshTokenCookie = CookieUtils.generateRefreshTokenCookie(REFRESH_TOKEN_COOKIE, refreshToken);
		httpServletResponse.addCookie(refreshTokenCookie);
	}

	private void expireRefreshTokenCookie(HttpServletResponse httpServletResponse) {
		Cookie refreshTokenCookie = CookieUtils.expireRefreshTokenCookie(REFRESH_TOKEN_COOKIE);
		httpServletResponse.addCookie(refreshTokenCookie);
	}
}
