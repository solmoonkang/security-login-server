package com.springoauth2.api.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateMemberRequest(
	@Email(message = "이메일 형식에 맞도록 작성해주세요.")
	@NotBlank(message = "이메일을 입력해주세요.")
	String email,

	@NotBlank(message = "닉네임을 입력해주세요.")
	String nickname,

	@NotBlank(message = "비밀번호를 입력해주세요.")
	String password,

	@NotBlank(message = "확인 비밀번호를 입력해주세요.")
	String checkPassword,

	String blog,

	String introduce
) {
}
