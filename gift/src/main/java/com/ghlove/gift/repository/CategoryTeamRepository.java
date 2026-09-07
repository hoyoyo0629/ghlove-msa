package com.ghlove.gift.repository;

import com.ghlove.gift.domain.CategoryTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryTeamRepository extends JpaRepository<CategoryTeam, Integer> {
    List<CategoryTeam> findAllByOrderByOrderingAscCategoryTeamIdAsc();

    boolean existsByCode(String code);
}
