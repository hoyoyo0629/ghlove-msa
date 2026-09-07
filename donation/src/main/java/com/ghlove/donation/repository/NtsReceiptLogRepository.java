package com.ghlove.donation.repository;

import com.ghlove.donation.domain.NtsReceiptLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NtsReceiptLogRepository extends JpaRepository<NtsReceiptLog, Long> {
    List<NtsReceiptLog> findByLogTypeOrderByLogSnDesc(String logType);
}
