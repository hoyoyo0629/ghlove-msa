package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntySrBbs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmntySrBbsRepository extends JpaRepository<CmntySrBbs, Long> {
    List<CmntySrBbs> findAllByOrderByBbsIdDesc();
}
