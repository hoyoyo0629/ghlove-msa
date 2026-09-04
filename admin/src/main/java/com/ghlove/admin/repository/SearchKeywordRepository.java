package com.ghlove.admin.repository;

import com.ghlove.admin.domain.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Integer> {
    List<SearchKeyword> findAllByOrderBySearchIdDesc();
}
