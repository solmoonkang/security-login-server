package com.springoauth2.api.application.auth;

import static com.springoauth2.global.util.Constant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import com.springoauth2.api.domain.auth.AuthMember;
import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.member.repositroy.MemberRepository;
import com.springoauth2.global.error.exception.NotFoundException;
import com.springoauth2.support.JwtFixture;
import com.springoauth2.support.MemberFixture;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

@SpringBootTest(classes = {JwtProviderService.class})
@TestPropertySource(properties = {
	"jwt.iss=test",
	"jwt.secret.access-key=test_test_test_test_test_test_test_test_test_test_test",
	"jwt.access-expire=60000",
	"jwt.refresh-expire=86400000"
})
class JwtProviderServiceTest {

	@Autowired
	JwtProviderService jwtProviderService;

	@MockBean
	MemberRepository memberRepository;

	SecretKey secretKey;

	@BeforeEach
	void setUp() {
		String salt = "test_test_test_test_test_test_test_test_test_test_test";
		secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("GENERATE ACCESS TOKEN(⭕️ SUCCESS): 액세스 토큰을 성공적으로 발급했습니다.")
	void generateAccessToken_accessToken_success() {
		// GIVEN
		String memberEmail = "solmoon@gmail.com";
		String memberNickname = "solmoon";

		// WHEN
		String accessToken = jwtProviderService.generateAccessToken(memberEmail, memberNickname);
		Jws<Claims> actual = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(accessToken);

		// THEN
		assertThat(actual.getPayload().get(MEMBER_EMAIL, String.class)).isEqualTo(memberEmail);
		assertThat(actual.getPayload().get(MEMBER_NICKNAME, String.class)).isEqualTo(memberNickname);
	}

	@Test
	@DisplayName("GENERATE REFRESH TOKEN(⭕️ SUCCESS): 리프레시 토큰을 성공적으로 발급했습니다.")
	void generateRefreshToken_refreshToken_success() {
		// GIVEN
		String memberEmail = "solmoon@gmail.com";

		// WHEN
		String refreshToken = jwtProviderService.generateRefreshToken(memberEmail);
		Jws<Claims> actual = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(refreshToken);

		// THEN
		assertThat(actual.getPayload().get(MEMBER_EMAIL, String.class)).isEqualTo(memberEmail);
	}

	@Test
	@DisplayName("REGENERATE ACCESS TOKEN(⭕️ SUCCESS): 리프레시 토큰을 통해 액세스 토큰을 성공적으로 재발급했습니다.")
	void reGenerateToken_accessToken_success() {
		// GIVEN
		Member member = MemberFixture.createMemberEntity();
		String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

		Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
		mockHttpServletRequest.setCookies(refreshTokenCookie);

		member.updateRefreshToken(refreshToken);

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.of(member));

		// WHEN
		String accessToken = jwtProviderService.reGenerateToken(refreshToken, mockHttpServletResponse);
		Jws<Claims> actual = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(accessToken);

		// THEN
		assertThat(actual.getPayload().get(MEMBER_EMAIL, String.class)).isEqualTo(member.getEmail());
		assertThat(actual.getPayload().get(MEMBER_NICKNAME, String.class)).isEqualTo(member.getNickname());

		Cookie[] cookies = mockHttpServletResponse.getCookies();
		assertThat(cookies).isNotEmpty();

		Cookie newRefreshTokenCookie = cookies[0];
		assertThat(newRefreshTokenCookie.getName()).isEqualTo(REFRESH_TOKEN_COOKIE);
		assertThat(newRefreshTokenCookie.getValue()).isNotEmpty();
		assertThat(newRefreshTokenCookie.getMaxAge()).isEqualTo(24 * 60 * 60);
		assertThat(newRefreshTokenCookie.isHttpOnly()).isTrue();
		assertThat(newRefreshTokenCookie.getPath()).isEqualTo("/");
	}

	@Test
	@DisplayName("REGENERATE ACCESS TOKEN(❌ FAIL): 리프레시 토큰에 해당하는 사용자 정보가 없습니다.")
	void reGenerateToken_member_NotFoundException_fail() {
		// GIVEN
		Member member = MemberFixture.createMemberEntity();
		String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

		member.updateRefreshToken(refreshToken);

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> jwtProviderService.reGenerateToken(refreshToken, mockHttpServletResponse))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다.");
	}

	@Test
	@DisplayName("REGENERATE ACCESS TOKEN(❌ FAIL): 해당 리프레시 토큰은 이미 재발급에 사용된 토큰입니다.")
	void reGenerateToken_used_NotFoundException_fail() {
		// GIVEN
		Member member = MemberFixture.createMemberEntity();
		String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

		member.updateRefreshToken("used" + refreshToken);

		given(memberRepository.findMemberByEmail(any(String.class))).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> jwtProviderService.reGenerateToken(refreshToken, mockHttpServletResponse))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("❎[ERROR] 요청하신 회원은 존재하지 않는 회원입니다.");
	}

	@Test
	@DisplayName("EXTRACT TOKEN(⭕️ SUCCESS): 요청한 토큰을 성공적으로 추출했습니다.")
	void extractToken_token_success() {
		// GIVEN
		String accessToken = "testAccessToken";

		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
		mockHttpServletRequest.addHeader(ACCESS_TOKEN_HEADER, BEARER + BLANK + accessToken);

		// WHEN
		String actual = jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, mockHttpServletRequest);

		// THEN
		assertThat(actual).isEqualTo(accessToken);
	}

	@Test
	@DisplayName("EXTRACT TOKEN(❌ FAIL): 요청한 토큰은 BEARER 타입이 아니거나 NULL 입니다.")
	void extractToken_not_bearer_or_null_fail() {
		// GIVEN
		String accessToken = "testAccessToken";

		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
		mockHttpServletRequest.addHeader(ACCESS_TOKEN_HEADER, accessToken);

		// WHEN
		String actual = jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, mockHttpServletRequest);

		// THEN
		assertThat(actual).isNull();
	}

	@Test
	@DisplayName("EXTRACT AUTH MEMBER BY ACCESS TOKEN(⭕️ SUCCESS): 요청한 토큰 정보를 추출해서 성공적으로 AuthMember를 생성했습니다.")
	void extractAuthMemberByAccessToken_authMember_success() {
		// GIVEN
		String memberEmail = "solmoon@gmail.com";
		String memberNickname = "solmoon";

		String accessToken = jwtProviderService.generateAccessToken(memberEmail, memberNickname);

		// WHEN
		AuthMember actualAuthMember = jwtProviderService.extractAuthMemberByAccessToken(accessToken);

		// THEN
		assertThat(actualAuthMember.email()).isEqualTo(memberEmail);
		assertThat(actualAuthMember.nickname()).isEqualTo(memberNickname);
	}

	@Test
	@DisplayName("IS USABLE(⭕️ SUCCESS): 해당 토큰이 유효한지에 대해서 성공적으로 확인했습니다.")
	void isUsable_valid_true_success() {
		// GIVEN
		String memberEmail = "solmoon@gmail.com";
		String memberNickname = "solmoon";

		String accessToken = jwtProviderService.generateAccessToken(memberEmail, memberNickname);

		// WHEN
		boolean actual = jwtProviderService.isUsable(accessToken);

		// THEN
		assertThat(actual).isTrue();
	}

	@Test
	@DisplayName("IS USABLE(❌ FAIL): 해당 토큰은 이미 만료된 토큰입니다.")
	void isUsable_expired_false_fail() {
		// GIVEN
		String accessToken = JwtFixture.createExpiredToken(secretKey);

		// WHEN
		boolean actual = jwtProviderService.isUsable(accessToken);

		// THEN
		assertThat(actual).isFalse();
	}

	@Test
	@DisplayName("IS USABLE(❌ FAIL): 해당 토큰은 비어 있는 토큰입니다.")
	void isUsable_emptied_NotFoundException_fail() {
		// WHEN & THEN
		assertThatThrownBy(() -> jwtProviderService.isUsable(""))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("❎[ERROR] JWT 토큰이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("IS USABLE(❌ FAIL): 해당 토큰은 잘못된 토큰입니다.")
	void isUsable_invalid_NotFoundException_fail() {
		// WHEN & THEN
		assertThatThrownBy(() -> jwtProviderService.isUsable("INVALID TOKEN"))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("❎[ERROR] 유효하지 않은 JWT 토큰입니다.");
	}
}