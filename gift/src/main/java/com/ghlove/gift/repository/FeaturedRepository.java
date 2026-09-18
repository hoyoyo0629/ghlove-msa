package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Featured;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FeaturedRepository extends JpaRepository<Featured, Integer> {
    List<Featured> findByFeaturedTypeOrderByFeaturedIdDesc(String featuredType);
    List<Featured> findByFeaturedTypeAndFeaturedNameContainingOrderByFeaturedIdDesc(String featuredType, String featuredName);

    /**
     * 지역이벤트 목록 - 진행중(AS-IS `conditionType='FRONT'`).
     *
     * <p>AS-IS `EventController.list()`(`:88~97`)의 조건을 옮겼다:
     * `featuredType='1'` + `featuredFlag='Y'`(사용) + `displayListFlag='Y'`(목록노출).
     * "진행중"은 AS-IS가 `conditionType='FRONT'`로 SQL에서 기간을 거르던 것을
     * 시작일 ≤ 오늘 ≤ 종료일로 표현했다(날짜가 비어 있으면 제한 없는 것으로 본다).
     */
    @Query("select f from Featured f where f.featuredType = :type and f.featuredFlag = 'Y'"
            + " and f.displayListFlag = 'Y'"
            + " and (f.startDate is null or f.startDate = '' or f.startDate <= :today)"
            + " and (f.endDate is null or f.endDate = '' or f.endDate >= :today)"
            + " order by f.ordering asc, f.featuredId desc")
    List<Featured> findOngoing(String type, String today);

    /**
     * 지역이벤트 목록 - 종료(AS-IS `ingCond='N'` → `progression='3'`).
     * 종료일이 오늘보다 이전인 것만.
     */
    @Query("select f from Featured f where f.featuredType = :type and f.featuredFlag = 'Y'"
            + " and f.displayListFlag = 'Y'"
            + " and f.endDate is not null and f.endDate <> '' and f.endDate < :today"
            + " order by f.endDate desc, f.featuredId desc")
    List<Featured> findEnded(String type, String today);

    /** 상세 진입은 AS-IS와 동일하게 `featuredUrl`로 찾는다 (`GET /api/event/{featuredUrl}`). */
    Optional<Featured> findByFeaturedUrlAndFeaturedFlag(String featuredUrl, String featuredFlag);
}
