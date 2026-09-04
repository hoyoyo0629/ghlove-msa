package com.ghlove.donation.repository;

import com.ghlove.donation.domain.LocgovDeptHist;
import com.ghlove.donation.domain.LocgovDeptHistId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocgovDeptHistRepository extends JpaRepository<LocgovDeptHist, LocgovDeptHistId> {
    List<LocgovDeptHist> findByLocgovCodeOrderByDeptHistNoDesc(String locgovCode);

    int countByLocgovCode(String locgovCode);
}
