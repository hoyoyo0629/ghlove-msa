package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 답례품 상품문의(Q&A). No AS-IS table fits: OP_SHOP_INQUIRY was assigned to
 * admin's DB by the batch DDL split and mixes in seller-onboarding fields
 * this doesn't need, so this is a new MVP-scoped table (G_ITEM_INQUIRY),
 * same rationale as order service's OD_CLAIM.
 */
@Entity
@Table(name = "G_ITEM_INQUIRY")
@Getter
@Setter
@NoArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INQUIRY_ID")
    private Long inquiryId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "QUESTION")
    private String question;

    @Column(name = "SECRET_YN")
    private String secretYn;

    @Column(name = "ANSWER")
    private String answer;

    @Column(name = "ANSWERED_DATE")
    private LocalDateTime answeredDate;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    /** 부적절한 문의 블라인드 처리(비노출)/복원 - Review.displayFlag와 동일한 관례. */
    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;
}
