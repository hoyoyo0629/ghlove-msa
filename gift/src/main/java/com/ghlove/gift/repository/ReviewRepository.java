package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItemIdAndDisplayFlagOrderByCreatedDateDesc(Long itemId, String displayFlag);

    List<Review> findByUserIdOrderByCreatedDateDesc(Long userId);

    int countByUserId(Long userId);
}
