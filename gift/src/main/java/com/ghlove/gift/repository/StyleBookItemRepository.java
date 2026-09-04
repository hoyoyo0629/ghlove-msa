package com.ghlove.gift.repository;

import com.ghlove.gift.domain.StyleBookItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StyleBookItemRepository extends JpaRepository<StyleBookItem, Long> {
    List<StyleBookItem> findByStyleBookIdOrderByOrderingAsc(Long styleBookId);

    void deleteByStyleBookIdAndItemId(Long styleBookId, Long itemId);

    void deleteByStyleBookId(Long styleBookId);
}
