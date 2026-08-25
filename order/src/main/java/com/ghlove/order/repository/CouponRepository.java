package com.ghlove.order.repository;

import com.ghlove.order.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    List<Coupon> findAllByOrderByCouponIdDesc();

    List<Coupon> findByCouponFlagAndTargetTimeType(String couponFlag, String targetTimeType);
}
