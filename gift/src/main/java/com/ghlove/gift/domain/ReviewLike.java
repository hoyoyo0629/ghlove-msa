package com.ghlove.gift.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 상품평 좋아요 (AS-IS `OP_ITEM_REVIEW_LIKE`).
 *
 * <p>AS-IS `ItemServiceImpl.saveItemReviewLike()`(`:5523~5553`)는 <b>취소가 없는 1회성</b>이다 -
 * 이미 누른 상태면 아무 일도 하지 않고 `false`를 돌려준다(토글이 아니다).
 * 중복 판정은 로그인 회원이면 `USER_ID`, 비로그인이면 `IP`로 한다.
 */
@Entity
@Table(name = "op_item_review_like")
@Getter
@Setter
@NoArgsConstructor
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ITEM_REVIEW_ID")
    private Integer itemReviewId;

    /** 비로그인 좋아요는 null. */
    @Column(name = "USER_ID")
    private Long userId;

    /** 비로그인 중복 판정 키. */
    @Column(name = "IP", length = 20)
    private String ip;

    @Column(name = "CREATED")
    private LocalDateTime created;
}
