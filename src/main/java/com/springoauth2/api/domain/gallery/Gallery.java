package com.springoauth2.api.domain.gallery;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.springoauth2.api.domain.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_galleries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gallery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "gallery_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Column(name = "title")
	private String title;

	@Column(name = "content")
	private String content;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "start_at")
	private LocalDateTime startAt;

	@Column(name = "end_at")
	private LocalDateTime endAt;

	@Column(name = "fee")
	private int fee;

	@Column(name = "review_average")
	private float reviewAverage;

	@Column(name = "thumbnail_uri")
	private String thumbnailUri;

	@Builder
	private Gallery(Member member, String title, String content, LocalDateTime createdAt, LocalDateTime startAt,
		LocalDateTime endAt, int fee, float reviewAverage, String thumbnailUri) {
		this.member = member;
		this.title = title;
		this.content = content;
		this.createdAt = createdAt;
		this.startAt = startAt;
		this.endAt = endAt;
		this.fee = fee;
		this.reviewAverage = reviewAverage;
		this.thumbnailUri = thumbnailUri;
	}
}
