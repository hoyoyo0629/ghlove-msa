package com.ghlove.donation.repository;

import com.ghlove.donation.domain.CtbnySetup;
import com.ghlove.donation.domain.CtbnySetupId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CtbnySetupRepository extends JpaRepository<CtbnySetup, CtbnySetupId> {
    Optional<CtbnySetup> findByStdrYearAndLocgovCode(String stdrYear, String locgovCode);
}
