package com.ghlove.gift.repository;

import com.ghlove.gift.domain.CategoryGroupBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryGroupBannerRepository extends JpaRepository<CategoryGroupBanner, Integer> {
    List<CategoryGroupBanner> findByCategoryGroupIdOrderByDisplayOrderAsc(Integer categoryGroupId);
}
