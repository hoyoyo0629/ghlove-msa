package com.ghlove.gift.repository;

import com.ghlove.gift.domain.FeaturedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedItemRepository extends JpaRepository<FeaturedItem, Long> {
    List<FeaturedItem> findByFeaturedIdOrderByDisplayOrder(Integer featuredId);
    void deleteByFeaturedId(Integer featuredId);
}
