package com.ghlove.gift.repository;

import com.ghlove.gift.domain.SeasonFood;
import com.ghlove.gift.domain.SeasonFoodId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeasonFoodRepository extends JpaRepository<SeasonFood, SeasonFoodId> {
    List<SeasonFood> findAllByOrderBySeasonFoodMonthAscRegSeqAsc();

    /** AS-IS SeasonalFoodMapper.deleteSeasonalFood - 그 달의 키워드 행을 전부 지운다. */
    @Modifying
    @Query("delete from SeasonFood s where s.seasonFoodMonth = :month")
    void deleteByMonth(@Param("month") Integer month);
}
