package com.ghlove.gift.repository;

import com.ghlove.gift.domain.GiftOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftOptionRepository extends JpaRepository<GiftOption, Long> {
    List<GiftOption> findByItemIdOrderByItemOptionIdAsc(Long itemId);

    List<GiftOption> findByItemIdAndOptionDisplayFlagOrderByItemOptionIdAsc(Long itemId, String optionDisplayFlag);
}
