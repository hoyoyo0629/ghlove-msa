package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

    List<Menu> findByDisplayFlagOrderByMenuSeq(String displayFlag);

    List<Menu> findAllByOrderByMenuParentIdAscMenuSeqAsc();

    List<Menu> findByMenuParentId(Integer menuParentId);

    Integer countByMenuParentId(Integer menuParentId);
}
