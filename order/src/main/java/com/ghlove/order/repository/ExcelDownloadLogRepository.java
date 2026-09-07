package com.ghlove.order.repository;

import com.ghlove.order.domain.ExcelDownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExcelDownloadLogRepository extends JpaRepository<ExcelDownloadLog, Long> {

    List<ExcelDownloadLog> findAllByOrderByDownloadLogIdDesc();
}
