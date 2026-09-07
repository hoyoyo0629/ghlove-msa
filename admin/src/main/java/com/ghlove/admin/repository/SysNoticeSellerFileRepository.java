package com.ghlove.admin.repository;

import com.ghlove.admin.domain.SysNoticeSellerFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysNoticeSellerFileRepository extends JpaRepository<SysNoticeSellerFile, Long> {
    List<SysNoticeSellerFile> findByNoticeIdOrderByAtchFileSeq(Long noticeId);
}
