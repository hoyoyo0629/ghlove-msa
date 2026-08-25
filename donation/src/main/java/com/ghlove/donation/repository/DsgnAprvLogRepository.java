package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DsgnAprvLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DsgnAprvLogRepository extends JpaRepository<DsgnAprvLog, Long> {
    List<DsgnAprvLog> findByDsgnDntnBizIdOrderByLogIdDesc(Long dsgnDntnBizId);
}
