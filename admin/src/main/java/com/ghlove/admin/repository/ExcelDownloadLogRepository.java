package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ExcelDownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** @deprecated {@link ExcelDownloadLog} 참고 - AS-IS에 없는 표이고 쓰이지 않는다. */
@Deprecated
public interface ExcelDownloadLogRepository extends JpaRepository<ExcelDownloadLog, Long> {

    List<ExcelDownloadLog> findAllByOrderByDownloadLogIdDesc();
}
