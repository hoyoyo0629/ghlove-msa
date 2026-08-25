package com.ghlove.donation.repository;

import com.ghlove.donation.domain.RelayLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelayLogRepository extends JpaRepository<RelayLog, Integer> {

    List<RelayLog> findAllByOrderByFrstRegistPnttmDesc();
}
