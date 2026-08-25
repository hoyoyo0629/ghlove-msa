package com.ghlove.order.repository;

import com.ghlove.order.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByCreatedDateDesc(Long userId);

    Optional<CartItem> findByUserIdAndItemId(Long userId, Long itemId);

    List<CartItem> findByCartItemIdInAndUserId(List<Long> cartItemIds, Long userId);

    void deleteByCartItemIdInAndUserId(List<Long> cartItemIds, Long userId);
}
