package com.springoauth2.api.domain.member.repositroy;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springoauth2.api.domain.member.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findMemberByEmail(String email);

	boolean existsMemberByEmail(String email);

	boolean existsMemberByNickname(String nickname);

	List<Member> findAllByNicknameIn(Set<String> nicknames);
}
