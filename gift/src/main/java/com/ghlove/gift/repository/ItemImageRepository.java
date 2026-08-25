package com.ghlove.gift.repository;

import com.ghlove.gift.domain.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    List<ItemImage> findByItemIdOrderByOrderingAsc(Long itemId);

    Optional<ItemImage> findFirstByItemIdOrderByOrderingAsc(Long itemId);

    List<ItemImage> findByItemIdInOrderByOrderingAsc(List<Long> itemIds);
}
