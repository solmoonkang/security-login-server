package com.springoauth2.api.domain.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springoauth2.api.domain.member.Member;
import com.springoauth2.api.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	List<Post> findPostByMember(Member member);
}
