package com.ghlove.gift.repository;

import com.ghlove.gift.domain.GiftOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftOptionRepository extends JpaRepository<GiftOption, Long> {
    List<GiftOption> findByItemIdOrderByItemOptionIdAsc(Long itemId);

    List<GiftOption> findByItemIdAndOptionDisplayFlagOrderByItemOptionIdAsc(Long itemId, String optionDisplayFlag);

    /** 목록 "옵션 전부 품절" 판정용 - 여러 답례품의 표시 옵션을 한 번에 조회. */
    List<GiftOption> findByItemIdInAndOptionDisplayFlag(List<Long> itemIds, String optionDisplayFlag);

    /** 조합형 저장 시 기존 옵션 전체 교체(AS-IS deleteItemOptionByItemId 패턴)용. */
    void deleteByItemId(Long itemId);
}
