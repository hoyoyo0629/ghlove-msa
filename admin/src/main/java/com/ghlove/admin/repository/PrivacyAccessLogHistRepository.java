package com.ghlove.admin.repository;

import com.ghlove.admin.domain.PrivacyAccessLogHist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivacyAccessLogHistRepository extends JpaRepository<PrivacyAccessLogHist, Long> {

    List<PrivacyAccessLogHist> findByPrivacyAccessLogIdOrderByHistIdDesc(Long privacyAccessLogId);

    long countByPrivacyAccessLogId(Long privacyAccessLogId);
}
