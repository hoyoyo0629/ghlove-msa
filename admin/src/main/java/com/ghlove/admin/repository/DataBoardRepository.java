package com.ghlove.admin.repository;

import com.ghlove.admin.domain.DataBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataBoardRepository extends JpaRepository<DataBoard, Integer> {
    List<DataBoard> findByUseYn(String useYn);
}
