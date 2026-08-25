package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Integer> {

    Optional<QnaAnswer> findByQnaId(Integer qnaId);

    List<QnaAnswer> findByQnaIdIn(List<Integer> qnaIds);
}
