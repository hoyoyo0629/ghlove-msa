package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedNoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesignatedNoticeFileRepository
        extends JpaRepository<DesignatedNoticeFile, DesignatedNoticeFile.Key> {

    List<DesignatedNoticeFile> findByNoticeIdOrderByFileSeqAsc(Long noticeId);

    Optional<DesignatedNoticeFile> findByNoticeIdAndFileSeq(Long noticeId, Integer fileSeq);

    void deleteByNoticeId(Long noticeId);
}
