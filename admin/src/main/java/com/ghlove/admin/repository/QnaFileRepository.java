package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QnaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaFileRepository extends JpaRepository<QnaFile, Integer> {

    List<QnaFile> findByQnaId(Integer qnaId);
}
