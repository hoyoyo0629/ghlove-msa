package com.ghlove.donation.repository;

import com.ghlove.donation.domain.SpecialDisasterZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialDisasterZoneRepository extends JpaRepository<SpecialDisasterZone, Long> {
    List<SpecialDisasterZone> findByLocgovCode(String locgovCode);
}
