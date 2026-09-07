package com.ghlove.gift.repository;

import com.ghlove.gift.domain.OpMobileCategoryEdit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpMobileCategoryEditRepository extends JpaRepository<OpMobileCategoryEdit, Integer> {
    List<OpMobileCategoryEdit> findByCodeOrderByEditPosition(String code);
    List<OpMobileCategoryEdit> findAllByOrderByCodeAscEditPositionAsc();
}
