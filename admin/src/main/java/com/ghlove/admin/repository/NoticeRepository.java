package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    List<Notice> findAllByOrderByNoticeIdDesc();

    List<Notice> findByUseYnOrderByNoticeIdDesc(String useYn);

    List<Notice> findByUseYnAndLocgovCodeOrderByNoticeIdDesc(String useYn, String locgovCode);
}
