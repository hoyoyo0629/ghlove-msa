package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Integer> {

    List<Ranking> findByCategoryUrlOrderByOrderingAsc(String categoryUrl);

    /** 랭킹 관리 화면의 "등록된 그룹 목록" 드롭다운용 - 실제 데이터가 있는 그룹만. */
    @org.springframework.data.jpa.repository.Query("select distinct r.categoryUrl from Ranking r")
    List<String> findDistinctCategoryUrls();

    boolean existsByCategoryUrlAndItemId(String categoryUrl, Integer itemId);
}
