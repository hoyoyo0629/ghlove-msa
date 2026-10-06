package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** 자료실 첨부파일 (AS-IS {@code G_CMNTY_FILE}). */
public interface CmntyFileRepository extends JpaRepository<CmntyFile, Long> {

    /** AS-IS getRpstrfileList - 살아있는 첨부만, 순번 순. */
    List<CmntyFile> findByRpstrIdAndUseYnOrderByAtchFileSeqAsc(Long rpstrId, String useYn);

    /** AS-IS insertRpstrFile의 {@code ifnull(max(atch_file_seq)+1, 1)}. */
    @Query("select coalesce(max(f.atchFileSeq), 0) + 1 from CmntyFile f where f.rpstrId = :rpstrId")
    int nextSeq(Long rpstrId);
}
