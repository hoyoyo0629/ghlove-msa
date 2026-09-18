package com.ghlove.gift.repository;

import com.ghlove.gift.domain.SeasonFoodItem;
import com.ghlove.gift.domain.SeasonFoodItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonFoodItemRepository extends JpaRepository<SeasonFoodItem, SeasonFoodItemId> {
    List<SeasonFoodItem> findBySeasonFoodMonth(Integer seasonFoodMonth);

    /** 답례품 수정화면의 "제철 월" 체크상태 복원용 (AS-IS itemMapper.getSeasonFoodItem). */
    List<SeasonFoodItem> findByItemIdOrderBySeasonFoodMonth(Long itemId);

    /** AS-IS는 저장할 때마다 해당 답례품의 행을 전부 지우고 다시 넣는다
     *  (ItemServiceImpl:1471 deleteSeasonFoodItem → insertSeasonFoodItem 반복). */
    void deleteByItemId(Long itemId);
}
