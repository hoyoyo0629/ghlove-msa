package com.ghlove.gift.repository;

import com.ghlove.gift.domain.SeasonFood;
import com.ghlove.gift.domain.SeasonFoodId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonFoodRepository extends JpaRepository<SeasonFood, SeasonFoodId> {
    List<SeasonFood> findAllByOrderBySeasonFoodMonthAscRegSeqAsc();
}
