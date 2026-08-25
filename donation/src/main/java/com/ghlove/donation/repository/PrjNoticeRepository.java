package com.ghlove.donation.repository;

import com.ghlove.donation.domain.PrjNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrjNoticeRepository extends JpaRepository<PrjNotice, Long> {
    List<PrjNotice> findByDsgnDntnBizIdOrderByPrjNoticeIdDesc(Long dsgnDntnBizId);
}
