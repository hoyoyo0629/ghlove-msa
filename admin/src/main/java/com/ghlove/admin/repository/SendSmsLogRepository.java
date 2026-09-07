package com.ghlove.admin.repository;

import com.ghlove.admin.domain.SendSmsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SendSmsLogRepository extends JpaRepository<SendSmsLog, Integer> {
    List<SendSmsLog> findAllByOrderBySendSmsLogIdDesc();
}
