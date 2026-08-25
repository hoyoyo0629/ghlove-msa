package com.ghlove.order.repository;

import com.ghlove.order.domain.CouponTargetItem;
import com.ghlove.order.domain.CouponTargetItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponTargetItemRepository extends JpaRepository<CouponTargetItem, CouponTargetItemId> {
    List<CouponTargetItem> findByCouponId(Integer couponId);

    boolean existsByCouponIdAndItemId(Integer couponId, Long itemId);

    void deleteByCouponId(Integer couponId);
}
