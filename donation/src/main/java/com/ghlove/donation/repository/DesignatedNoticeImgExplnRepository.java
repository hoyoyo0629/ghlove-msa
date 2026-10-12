package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedNoticeImgExpln;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignatedNoticeImgExplnRepository
        extends JpaRepository<DesignatedNoticeImgExpln, DesignatedNoticeImgExpln.Key> {

    List<DesignatedNoticeImgExpln> findByNoticeIdOrderByImgSeqAsc(Long noticeId);

    void deleteByNoticeId(Long noticeId);
}
