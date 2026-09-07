package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmntyCommentRepository extends JpaRepository<CmntyComment, Long> {
    List<CmntyComment> findByBoardTypeAndBbsIdOrderByFrstCrtDtAsc(String boardType, Long bbsId);

    void deleteByBoardTypeAndBbsId(String boardType, Long bbsId);
}
