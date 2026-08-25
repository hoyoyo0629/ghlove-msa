package com.ghlove.gift.repository;

import com.ghlove.gift.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByItemReviewIdOrderByOrderingAsc(Long itemReviewId);

    List<ReviewImage> findByItemReviewIdInOrderByOrderingAsc(List<Long> itemReviewIds);
}
