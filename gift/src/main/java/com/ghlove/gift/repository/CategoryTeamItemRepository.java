package com.ghlove.gift.repository;

import com.ghlove.gift.domain.CategoryTeamItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryTeamItemRepository extends JpaRepository<CategoryTeamItem, Integer> {
    List<CategoryTeamItem> findByCategoryTeamIdOrderByCategoryTeamItemIdDesc(Integer categoryTeamId);

    void deleteByCategoryTeamIdAndItemId(Integer categoryTeamId, Long itemId);
}
