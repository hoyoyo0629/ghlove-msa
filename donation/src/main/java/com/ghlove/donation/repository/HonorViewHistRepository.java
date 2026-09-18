package com.ghlove.donation.repository;

import com.ghlove.donation.domain.HonorViewHist;
import org.springframework.data.jpa.repository.JpaRepository;

/** 기부혜택증 열람 이력 (AS-IS `OP_HONOR_VIEW_HIST`). */
public interface HonorViewHistRepository extends JpaRepository<HonorViewHist, HonorViewHist.Id> {
}
