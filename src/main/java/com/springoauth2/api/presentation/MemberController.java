package com.springoauth2.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springoauth2.api.application.MemberService;
import com.springoauth2.api.dto.CreateMemberRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<String> signUp(@RequestBody @Validated CreateMemberRequest createMemberRequest) {
		memberService.signUp(createMemberRequest);
		return ResponseEntity.ok("SUCCESS SIGN UP");
	}
}
