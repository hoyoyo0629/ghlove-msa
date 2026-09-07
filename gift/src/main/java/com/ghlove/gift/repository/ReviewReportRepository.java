package com.ghlove.gift.repository;

import com.ghlove.gift.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByItemReviewIdAndUserId(Long itemReviewId, Long userId);

    List<ReviewReport> findByItemReviewIdIn(List<Long> itemReviewIds);
}
