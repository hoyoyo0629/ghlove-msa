package com.ghlove.order.repository;

import com.ghlove.order.domain.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Integer> {
    List<CouponIssue> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<CouponIssue> findByCouponIdAndUserId(Integer couponId, Long userId);

    List<CouponIssue> findByCouponIdOrderByCreatedDateDesc(Integer couponId);

    long countByCouponId(Integer couponId);

    long countByCouponIdAndUserId(Integer couponId, Long userId);

    Optional<CouponIssue> findByCouponUserIdAndUserId(Integer couponUserId, Long userId);
}
