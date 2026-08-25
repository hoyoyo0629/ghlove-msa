package com.ghlove.gift.repository;

import com.ghlove.gift.domain.SeasonFoodItem;
import com.ghlove.gift.domain.SeasonFoodItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonFoodItemRepository extends JpaRepository<SeasonFoodItem, SeasonFoodItemId> {
    List<SeasonFoodItem> findBySeasonFoodMonth(Integer seasonFoodMonth);
}
