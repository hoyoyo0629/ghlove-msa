package com.ghlove.admin.repository;

import com.ghlove.admin.domain.SysNoticeSeller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysNoticeSellerRepository extends JpaRepository<SysNoticeSeller, Long> {
    List<SysNoticeSeller> findAllByOrderByNoticeIdDesc();
}
