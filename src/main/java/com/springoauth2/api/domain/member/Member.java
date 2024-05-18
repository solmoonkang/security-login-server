package com.springoauth2.api.domain.member;

import com.springoauth2.api.dto.CreateMemberRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "email")
	private String email;

	@Column(name = "nickname")
	private String nickname;

	@Column(name = "password")
	private String password;

	@Column(name = "blog")
	private String blog;

	@Column(name = "introduce")
	private String introduce;

	@Column(name = "refresh_token")
	private String refreshToken;

	@Builder
	private Member(String email, String nickname, String password, String blog, String introduce) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.blog = blog;
		this.introduce = introduce;
	}

	public static Member createMember(CreateMemberRequest createMemberRequest, String password) {
		return Member.builder()
			.email(createMemberRequest.email())
			.nickname(createMemberRequest.nickname())
			.password(password)
			.blog(createMemberRequest.blog())
			.introduce(createMemberRequest.introduce())
			.build();
	}

	public void updateRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
