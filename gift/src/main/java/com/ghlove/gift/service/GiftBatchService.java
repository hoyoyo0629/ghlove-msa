package com.ghlove.gift.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** gift 배치 본문 - AS-IS ghlove-batch JobServiceImpl의 답례품 관련 job. SQL은 AS-IS 매퍼 그대로. */
@Service
@RequiredArgsConstructor
public class GiftBatchService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final EntityManager entityManager;

    /**
     * itemDisplayContorlBatch(마감일자 공개여부 처리) - AS-IS ngdonation-mapper {@code getItemDisplayCheckList} +
     * {@code updateItemDisplayFlag}: 마감일시(ITEM_CLOSE_DT) 날짜가 어제인 답례품 중 공개(Y)인 것을 비공개(N)로.
     */
    @Transactional
    public String closeDisplayOfEndedItems() {
        int updated = entityManager.createNativeQuery("update op_item set display_flag = 'N' "
                        + "where substr(item_close_dt, 1, 8) = :yesterday and upper(display_flag) = 'Y'")
                .setParameter("yesterday", LocalDate.now().minusDays(1).format(YMD))
                .executeUpdate();
        return "비공개 " + updated + "건";
    }

    /**
     * deleteItemNewBatch(상품 NEW 삭제) - AS-IS item-mapper {@code deleteItemNew}: NEW 라벨(ITEM_LABEL '2')·신상품(Y)인
     * 답례품 중 등록일이 15일 전 날짜까지인 것을 일반상품으로 되돌린다(등록일은 yyyyMMddHHmmss 문자열 비교 그대로).
     */
    @Transactional
    public String clearNewLabel() {
        int updated = entityManager.createNativeQuery("update op_item set item_label = '0', item_new_flag = 'N' "
                        + "where item_label = '2' and item_new_flag = 'Y' and created_date <= :until")
                .setParameter("until", LocalDate.now().minusDays(15).format(YMD) + "999999")
                .executeUpdate();
        return "NEW 해제 " + updated + "건";
    }
}
