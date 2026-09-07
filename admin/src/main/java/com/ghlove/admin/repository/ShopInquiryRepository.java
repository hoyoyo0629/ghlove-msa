package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ShopInquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopInquiryRepository extends JpaRepository<ShopInquiry, Integer> {
    List<ShopInquiry> findAllByOrderByInquiryIdDesc();
}
