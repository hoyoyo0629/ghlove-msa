package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OpHonorViewHist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpHonorViewHistRepository extends JpaRepository<OpHonorViewHist, Long> {
    List<OpHonorViewHist> findAllByOrderByViewDtDesc();
}
