package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    List<Wishlist> findByUserIdOrderByCreatedDateDesc(Long userId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    Optional<Wishlist> findByWishlistIdAndUserId(Integer wishlistId, Long userId);

    void deleteByWishlistIdInAndUserId(List<Integer> wishlistIds, Long userId);

    void deleteByUserIdAndItemId(Long userId, Long itemId);

    int countByUserId(Long userId);
}
