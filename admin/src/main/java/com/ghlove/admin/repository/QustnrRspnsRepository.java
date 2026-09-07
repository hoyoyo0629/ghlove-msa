package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QustnrRspns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QustnrRspnsRepository extends JpaRepository<QustnrRspns, Long> {
    long countByQustnrSn(Long qustnrSn);
}
