package com.ghlove.admin.repository;

import com.ghlove.admin.domain.UmsSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UmsSendLogRepository extends JpaRepository<UmsSendLog, Long> {
    List<UmsSendLog> findAllByOrderByIdDesc();

    long countBySuccessYn(String successYn);

    long countByRetryCountGreaterThan(int retryCount);
}
