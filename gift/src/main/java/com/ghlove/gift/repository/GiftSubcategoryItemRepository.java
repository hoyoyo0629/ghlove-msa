package com.ghlove.gift.repository;

import com.ghlove.gift.domain.GiftSubcategoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftSubcategoryItemRepository extends JpaRepository<GiftSubcategoryItem, Long> {
    List<GiftSubcategoryItem> findAllByOrderBySubcategoryIdAscOrderingAsc();
}
