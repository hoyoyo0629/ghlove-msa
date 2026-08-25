package com.ghlove.point.repository;

import com.ghlove.point.domain.LocgovPointRate;
import com.ghlove.point.domain.LocgovPointRateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocgovPointRateRepository extends JpaRepository<LocgovPointRate, LocgovPointRateId> {
    Optional<LocgovPointRate> findByStdrYearAndLocgovCode(String stdrYear, String locgovCode);
}
