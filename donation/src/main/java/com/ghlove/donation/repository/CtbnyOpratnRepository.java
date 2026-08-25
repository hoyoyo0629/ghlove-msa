package com.ghlove.donation.repository;

import com.ghlove.donation.domain.CtbnyOpratn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CtbnyOpratnRepository extends JpaRepository<CtbnyOpratn, Long> {

    List<CtbnyOpratn> findByLocgovCodeOrderByExpndtrDeDesc(String locgovCode);
}
