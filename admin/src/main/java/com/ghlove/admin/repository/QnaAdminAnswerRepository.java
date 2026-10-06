package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QnaAdminAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QnaAdminAnswerRepository extends JpaRepository<QnaAdminAnswer, Long> {

    /** 문의 1건의 (정상) 답변. 문의당 답변 1건 관례라 최신 1건을 답변으로 본다. */
    Optional<QnaAdminAnswer> findFirstByQnaAdminIdAndDataStatusCodeOrderByAnswerDateDesc(Long qnaAdminId, String dataStatusCode);
}
