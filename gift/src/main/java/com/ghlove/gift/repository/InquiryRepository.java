package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByItemIdOrderByCreatedDateDesc(Long itemId);

    List<Inquiry> findByItemIdInOrderByCreatedDateDesc(List<Long> itemIds);

    List<Inquiry> findByUserIdOrderByCreatedDateDesc(Long userId);

    int countByUserId(Long userId);
}
