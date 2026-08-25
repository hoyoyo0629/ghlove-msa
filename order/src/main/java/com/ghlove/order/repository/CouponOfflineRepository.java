package com.ghlove.order.repository;

import com.ghlove.order.domain.CouponOffline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponOfflineRepository extends JpaRepository<CouponOffline, Integer> {
    List<CouponOffline> findByCouponIdOrderByCouponOfflineIdDesc(Integer couponId);

    Optional<CouponOffline> findByOfflineCode(String offlineCode);
}
