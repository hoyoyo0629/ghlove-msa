package com.ghlove.order.repository;

import com.ghlove.order.domain.CouponRegular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRegularRepository extends JpaRepository<CouponRegular, Integer> {
    List<CouponRegular> findAllByOrderByCouponIdDesc();

    List<CouponRegular> findByCouponFlag(String couponFlag);
}
