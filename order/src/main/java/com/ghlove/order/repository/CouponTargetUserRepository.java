package com.ghlove.order.repository;

import com.ghlove.order.domain.CouponTargetUser;
import com.ghlove.order.domain.CouponTargetUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponTargetUserRepository extends JpaRepository<CouponTargetUser, CouponTargetUserId> {
    List<CouponTargetUser> findByCouponId(Integer couponId);

    boolean existsByCouponIdAndUserId(Integer couponId, Long userId);

    void deleteByCouponId(Integer couponId);
}
