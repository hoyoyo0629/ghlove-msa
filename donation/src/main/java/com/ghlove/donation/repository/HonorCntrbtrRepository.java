package com.ghlove.donation.repository;

import com.ghlove.donation.domain.HonorCntrbtr;
import com.ghlove.donation.domain.HonorCntrbtrId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HonorCntrbtrRepository extends JpaRepository<HonorCntrbtr, HonorCntrbtrId> {

    List<HonorCntrbtr> findByUserIdOrderByStdrYearDesc(Long userId);

    Optional<HonorCntrbtr> findByStdrYearAndLocgovCodeAndUserId(Integer stdrYear, String locgovCode, Long userId);
}
