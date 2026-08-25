package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Wishlist;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 관심답례품 (마이페이지 "관심답례품" - 찜하기/찜 목록/찜 해제). */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WishlistRepository wishlistRepository;
    private final GiftRepository giftRepository;

    @Transactional
    public void add(Long userId, Long itemId) {
        if (wishlistRepository.existsByUserIdAndItemId(userId, itemId)) {
            return;
        }
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setItemId(itemId);
        wishlist.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        wishlistRepository.save(wishlist);
    }

    /** AS-IS mypage/favorItem.html은 개별 삭제 버튼 없이 체크박스로 골라 "선택 삭제"만 지원한다. */
    @Transactional
    public void removeAll(List<Integer> wishlistIds, Long userId) {
        wishlistRepository.deleteByWishlistIdInAndUserId(wishlistIds, userId);
    }

    /** 답례품 목록 카드의 하트 아이콘 토글(AS-IS goods/index-main.html addToWishList).
     * @return 토글 후 상태 - true면 관심답례품에 추가된 상태. */
    @Transactional
    public boolean toggle(Long userId, Long itemId) {
        if (wishlistRepository.existsByUserIdAndItemId(userId, itemId)) {
            wishlistRepository.deleteByUserIdAndItemId(userId, itemId);
            return false;
        }
        add(userId, itemId);
        return true;
    }

    /** 답례품 정보(이름/가격/판매상태/이미지)까지 합류한 관심답례품 목록. 삭제된 답례품은 조용히 제외한다. */
    public List<WishlistItemView> wishlistOf(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByCreatedDateDesc(userId);
        Map<Long, Gift> giftsById = new LinkedHashMap<>();
        for (Gift g : giftRepository.findAllById(wishlists.stream().map(Wishlist::getItemId).toList())) {
            giftsById.put(g.getItemId(), g);
        }
        return wishlists.stream()
                .filter(w -> giftsById.containsKey(w.getItemId()))
                .map(w -> new WishlistItemView(w.getWishlistId(), giftsById.get(w.getItemId())))
                .toList();
    }

    public record WishlistItemView(Integer wishlistId, Gift gift) {
    }

    /** 답례품 목록 카드의 하트(관심답례품) 아이콘 on/off 표시용. */
    public java.util.Set<Long> wishlistedItemIds(Long userId, List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return java.util.Set.of();
        }
        return wishlistRepository.findByUserIdOrderByCreatedDateDesc(userId).stream()
                .map(Wishlist::getItemId)
                .filter(itemIds::contains)
                .collect(java.util.stream.Collectors.toSet());
    }
}
