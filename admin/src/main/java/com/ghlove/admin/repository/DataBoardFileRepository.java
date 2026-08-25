package com.ghlove.admin.repository;

import com.ghlove.admin.domain.DataBoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataBoardFileRepository extends JpaRepository<DataBoardFile, String> {
    List<DataBoardFile> findByDataIdOrderByOrdering(Integer dataId);
}
