package com.ghlove.gift.repository;

import com.ghlove.gift.domain.MainDisplayItem;
import com.ghlove.gift.domain.MainDisplayItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MainDisplayItemRepository extends JpaRepository<MainDisplayItem, MainDisplayItemId> {
    List<MainDisplayItem> findByTemplateIdOrderByDisplayOrderAsc(String templateId);

    void deleteByTemplateId(String templateId);
}
