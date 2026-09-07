package com.ghlove.gift.repository;

import com.ghlove.gift.domain.InquiryReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReportRepository extends JpaRepository<InquiryReport, Long> {
    boolean existsByInquiryIdAndUserId(Long inquiryId, Long userId);

    List<InquiryReport> findByInquiryIdIn(List<Long> inquiryIds);
}
