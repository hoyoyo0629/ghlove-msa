package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QustnrRspns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QustnrRspnsRepository extends JpaRepository<QustnrRspns, Long> {
    long countByQustnrSn(Long qustnrSn);

    /** 1인 1회 응답 - AS-IS getQustnrByApi가 regCnt>0이면 FAIL_ALREADY_DONE으로 막는다. */
    boolean existsByQustnrSnAndUserId(Long qustnrSn, Long userId);
}
