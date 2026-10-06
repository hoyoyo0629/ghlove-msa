package com.ghlove.order.repository;

import com.ghlove.order.domain.Island;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IslandRepository extends JpaRepository<Island, Long> {

    /**
     * 우편번호로 island_type(JEJU/ISLAND)을 조회한다 - AS-IS order-mapper.xml getIslandTypeByZipcode:
     * {@code REPLACE(ZIPCODE,'-','') = REPLACE(#{value},'-','') ORDER BY ID DESC LIMIT 1} 그대로.
     * 매칭 없으면 빈 리스트(추가배송비 없음).
     */
    @Query("select i.islandType from Island i "
            + "where replace(i.zipcode, '-', '') = replace(:zipcode, '-', '') order by i.id desc")
    List<String> findIslandTypesByZipcode(@Param("zipcode") String zipcode, PageRequest pageRequest);

    default String islandTypeByZipcode(String zipcode) {
        if (zipcode == null || zipcode.isBlank()) {
            return "";
        }
        List<String> types = findIslandTypesByZipcode(zipcode, PageRequest.of(0, 1));
        return types.isEmpty() ? "" : types.get(0);
    }
}
