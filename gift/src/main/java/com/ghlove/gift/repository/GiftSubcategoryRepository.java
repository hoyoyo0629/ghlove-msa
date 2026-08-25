package com.ghlove.gift.repository;

import com.ghlove.gift.domain.GiftSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftSubcategoryRepository extends JpaRepository<GiftSubcategory, Long> {
    List<GiftSubcategory> findAllByOrderByCategoryCodeAscOrderingAsc();
}
