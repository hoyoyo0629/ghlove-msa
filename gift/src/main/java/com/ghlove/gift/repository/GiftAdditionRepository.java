package com.ghlove.gift.repository;

import com.ghlove.gift.domain.GiftAddition;
import com.ghlove.gift.domain.GiftAdditionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 추가구성(op_item_addition) 조회/편성. */
public interface GiftAdditionRepository extends JpaRepository<GiftAddition, GiftAdditionId> {

    List<GiftAddition> findByItemId(Long itemId);

    void deleteByItemId(Long itemId);
}
