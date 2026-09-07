package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ExcelDownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExcelDownloadLogRepository extends JpaRepository<ExcelDownloadLog, Long> {

    List<ExcelDownloadLog> findAllByOrderByDownloadLogIdDesc();
}
