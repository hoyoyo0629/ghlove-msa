package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QnaAdminFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaAdminFileRepository extends JpaRepository<QnaAdminFile, Long> {

    /** AS-IS getQnaAdminFileList - 문의 한 건의 첨부(정렬 조건 없음 - 번호순으로 둔다). */
    List<QnaAdminFile> findByQnaAdminIdOrderByQnaAdminFileIdAsc(Long qnaAdminId);
}
