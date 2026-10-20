package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QnaAdminAnswerFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaAdminAnswerFileRepository extends JpaRepository<QnaAdminAnswerFile, Long> {

    /** AS-IS getQnaAdminAnswerFileList - 답변 한 건의 첨부(정렬 조건 없음 - 번호순으로 둔다). */
    List<QnaAdminAnswerFile> findByQnaAdminAnswerIdOrderByQnaAdminAnswerFileIdAsc(Long qnaAdminAnswerId);
}
