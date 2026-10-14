package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedNoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesignatedNoticeFileRepository
        extends JpaRepository<DesignatedNoticeFile, DesignatedNoticeFile.Key> {

    List<DesignatedNoticeFile> findByNoticeIdOrderByFileSeqAsc(Long noticeId);

    /** 대민 공지 첨부 목록 - AS-IS selectDesignatedDonationNoticeFileList(ORDER BY FILE_SEQ DESC). */
    List<DesignatedNoticeFile> findByNoticeIdOrderByFileSeqDesc(Long noticeId);

    Optional<DesignatedNoticeFile> findFirstByNoticeIdAndFileNm(Long noticeId, String fileNm);

    Optional<DesignatedNoticeFile> findByNoticeIdAndFileSeq(Long noticeId, Integer fileSeq);

    void deleteByNoticeId(Long noticeId);
}
