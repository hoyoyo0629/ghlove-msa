package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaRepository extends JpaRepository<Qna, Integer> {

    List<Qna> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<Qna> findAllByOrderByCreatedDateDesc();
}
