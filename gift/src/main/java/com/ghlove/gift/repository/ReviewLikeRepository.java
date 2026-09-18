package com.ghlove.gift.repository;

import com.ghlove.gift.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품평 좋아요 (AS-IS `OP_ITEM_REVIEW_LIKE`). */
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByItemReviewIdAndUserId(Integer itemReviewId, Long userId);

    /** 비로그인 좋아요는 IP로 중복을 본다 (AS-IS와 동일). */
    boolean existsByItemReviewIdAndUserIdIsNullAndIp(Integer itemReviewId, String ip);
}
