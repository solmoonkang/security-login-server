package com.springoauth2.support;

import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.dto.CreateMemberRequest;

public class MemberFixture {

	public static Member createMemberEntity() {
		return Member.createMember(
			createMemberEntityForCreateMemberRequest(),
			"1q2w3e4r!");
	}

	public static CreateMemberRequest createMemberRequest() {
		return CreateMemberRequest.builder()
			.email("solmoon@gmail.com")
			.nickname("solmoon")
			.password("1q2w3e4r!")
			.checkPassword("1q2w3e4r!")
			.blog("https://www.solmoon.com")
			.introduce("Hello 👏")
			.build();
	}

	public static CreateMemberRequest createMemberRequestWithDifferentPassword() {
		return CreateMemberRequest.builder()
			.email("solmoon@gmail.com")
			.nickname("solmoon")
			.password("1q2w3e4r!")
			.checkPassword("1q2w3e4r!wrong")
			.blog("https://www.solmoon.com")
			.introduce("Hello 👏")
			.build();
	}

	public static CreateMemberRequest createMemberEntityForCreateMemberRequest() {
		return CreateMemberRequest.builder()
			.email("solmoon@gmail.com")
			.nickname("solmoon")
			.blog("https://www.solmoon.com")
			.introduce("Hello 👏")
			.build();
	}
}
