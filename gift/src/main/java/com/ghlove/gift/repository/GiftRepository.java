package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Gift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftRepository extends JpaRepository<Gift, Long> {
    List<Gift> findByDataStatusCodeAndDisplayFlagOrderByItemIdDesc(String dataStatusCode, String displayFlag);

    List<Gift> findByDataStatusCodeAndDisplayFlagAndCategoryCodeOrderByItemIdDesc(
            String dataStatusCode, String displayFlag, String categoryCode);

    List<Gift> findBySellerIdOrderByItemIdDesc(Long sellerId);

    List<Gift> findByDataStatusCodeOrderByItemIdDesc(String dataStatusCode);

    List<Gift> findByDataStatusCodeAndDisplayFlagAndItemNameContainingIgnoreCaseOrderByItemIdDesc(
            String dataStatusCode, String displayFlag, String keyword);

    List<Gift> findByDataStatusCodeAndDisplayFlagAndLocgovCodeOrderByItemIdDesc(
            String dataStatusCode, String displayFlag, String locgovCode);

    List<Gift> findByRepresentativeItemYnOrderByItemIdDesc(String representativeItemYn);
}
