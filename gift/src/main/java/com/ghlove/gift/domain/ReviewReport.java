package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 답례품 리뷰 신고 (SFR-005 "고객 후기/문의 관리(...신고)"). */
@Entity
@Table(name = "ITEM_REVIEW_REPORT")
@Getter
@Setter
@NoArgsConstructor
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPORT_ID")
    private Long reportId;

    @Column(name = "ITEM_REVIEW_ID")
    private Long itemReviewId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
