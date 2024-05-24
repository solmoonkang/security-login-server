package com.springoauth2.api.application.auth;

import static com.springoauth2.global.util.Constant.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.global.error.exception.NotFoundException;
import com.springoauth2.global.util.CookieUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProviderService {

	@Value("${jwt.iss}")
	private String iss;

	@Value("${jwt.secret.access-key}")
	private String salt;

	@Value("${jwt.access-expire}")
	private long accessExpire;

	@Value("${jwt.refresh-expire}")
	private long refreshExpire;

	private SecretKey secretKey;
	private final MemberRepository memberRepository;

	@PostConstruct
	private void init() {
		secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(String email, String nickname) {
		final Date issuedDate = new Date();
		final Date expiredDate = new Date(issuedDate.getTime() + accessExpire);

		return buildJwt(issuedDate, expiredDate)
			.claim(MEMBER_EMAIL, email)
			.claim(MEMBER_NICKNAME, nickname)
			.compact();
	}

	public String generateRefreshToken(String email) {
		final Date issuedDate = new Date();
		final Date expiredDate = new Date(issuedDate.getTime() + refreshExpire);

		return buildJwt(issuedDate, expiredDate)
			.claim(MEMBER_EMAIL, email)
			.compact();
	}

	@Transactional
	public String reGenerateToken(String refreshToken, HttpServletResponse httpServletResponse) {
		final Claims claims = getClaimsByToken(refreshToken);
		final String userEmail = claims.get(MEMBER_EMAIL, String.class);
		final Member member = memberRepository.findMemberByEmail(userEmail)
			.orElseThrow(() -> new NotFoundException("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다."));

		validateRefreshToken(refreshToken, member.getRefreshToken());

		final String newAccessToken = generateAccessToken(member.getEmail(), member.getNickname());
		final String newRefreshToken = generateRefreshToken(member.getEmail());

		member.updateRefreshToken(newRefreshToken);
		httpServletResponse.setHeader(ACCESS_TOKEN_HEADER, newAccessToken);

		Cookie refreshTokenCookie = CookieUtils.generateRefreshTokenCookie(REFRESH_TOKEN_COOKIE, newRefreshToken);
		httpServletResponse.addCookie(refreshTokenCookie);

		return newAccessToken;
	}

	public String extractToken(String header, HttpServletRequest httpServletRequest) {
		String token = httpServletRequest.getHeader(header);

		if (token == null || !token.startsWith(BEARER)) {
			log.warn("{} IS NULL OR NOT BEARER", header);
			return null;
		}

		return token.replaceFirst(BEARER, "").trim();
	}

	public AuthMember extractAuthMemberByAccessToken(String token) {
		final Claims claims = getClaimsByToken(token);
		final String memberEmail = claims.get(MEMBER_EMAIL, String.class);
		final String memberNickname = claims.get(MEMBER_NICKNAME, String.class);

		return AuthMember.createAuthMember(memberEmail, memberNickname);
	}

	public boolean isUsable(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token);

			return true;
		} catch (ExpiredJwtException e) {
			log.warn("EXPIRED JWT: JWT 토큰이 만료되었습니다.");
		} catch (IllegalArgumentException e) {
			log.warn("EMPTIED JWT: JWT 토큰이 존재하지 않습니다.");
			throw new NotFoundException("❎[ERROR] JWT 토큰이 존재하지 않습니다.");
		} catch (Exception e) {
			log.warn("INVALID TOKEN: 유효하지 않은 토큰입니다.");
			throw new NotFoundException("❎[ERROR] 유효하지 않은 JWT 토큰입니다.");
		}

		return false;
	}

	private void validateRefreshToken(String currentRefreshToken, String savedRefreshToken) {
		if (!currentRefreshToken.equals(savedRefreshToken)) {
			log.warn("INVALID REFRESH TOKEN: 유효하지 않은 리프레시 토큰입니다.");
			throw new NotFoundException("❎[ERROR] 유효하지 않은 리프레시 토큰입니다.");
		}
	}

	private JwtBuilder buildJwt(Date issuedDate, Date expiredDate) {
		return Jwts.builder()
			.issuer(iss)
			.issuedAt(issuedDate)
			.expiration(expiredDate)
			.signWith(secretKey, SignatureAlgorithm.HS256);
	}

	private Claims getClaimsByToken(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
