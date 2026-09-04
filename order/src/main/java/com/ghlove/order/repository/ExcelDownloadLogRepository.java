package com.ghlove.order.repository;

import com.ghlove.order.domain.ExcelDownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcelDownloadLogRepository extends JpaRepository<ExcelDownloadLog, Long> {
}
