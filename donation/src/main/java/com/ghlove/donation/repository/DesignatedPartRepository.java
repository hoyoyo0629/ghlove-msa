package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignatedPartRepository extends JpaRepository<DesignatedPart, Long> {
    List<DesignatedPart> findByLocgovCodeOrderByDeptIdDesc(String locgovCode);

    List<DesignatedPart> findAllByOrderByDeptIdDesc();
}
