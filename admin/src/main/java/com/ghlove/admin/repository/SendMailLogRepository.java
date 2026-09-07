package com.ghlove.admin.repository;

import com.ghlove.admin.domain.SendMailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SendMailLogRepository extends JpaRepository<SendMailLog, Integer> {
    List<SendMailLog> findAllByOrderBySendMailLogIdDesc();
}
