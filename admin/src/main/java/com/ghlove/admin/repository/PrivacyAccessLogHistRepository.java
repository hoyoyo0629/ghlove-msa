package com.ghlove.admin.repository;

import com.ghlove.admin.domain.PrivacyAccessLogHist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivacyAccessLogHistRepository extends JpaRepository<PrivacyAccessLogHist, Long> {

    /** AS-IS privacy-access-mapper.getPrivacyAccessLogHistListByParam - ORDER BY CREATED_AT DESC
     *  (HIST_ID 아님). HIST_ID는 채번 순서일 뿐 등록일시와 반드시 같은 방향이 아니다. */
    List<PrivacyAccessLogHist> findByPrivacyAccessLogIdOrderByCreatedAtDesc(Long privacyAccessLogId);

    long countByPrivacyAccessLogId(Long privacyAccessLogId);
}
