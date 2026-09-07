package com.ghlove.admin.repository;

import com.ghlove.admin.domain.UmsDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UmsDetailRepository extends JpaRepository<UmsDetail, Long> {
    List<UmsDetail> findByUmsIdOrderByUmsType(Long umsId);

    void deleteByUmsId(Long umsId);
}
