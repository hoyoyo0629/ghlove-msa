package com.ghlove.gift.repository;

import com.ghlove.gift.domain.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Integer> {
    List<CategoryGroup> findByCategoryTeamIdOrderByOrderingAscCategoryGroupIdAsc(Integer categoryTeamId);

    List<CategoryGroup> findAllByOrderByOrderingAscCategoryGroupIdAsc();
}
