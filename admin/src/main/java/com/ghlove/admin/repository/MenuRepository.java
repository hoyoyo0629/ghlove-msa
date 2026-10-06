package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

    List<Menu> findByDisplayFlagOrderByMenuSeq(String displayFlag);

    /** AS-IS menu-mapper.xml의 모든 메뉴 조회가 거는 조건(STATUS_CODE='1')과 동일 - 사용중인 메뉴. */
    List<Menu> findByStatusCodeOrderByMenuSeq(String statusCode);

    List<Menu> findAllByOrderByMenuParentIdAscMenuSeqAsc();

    List<Menu> findByMenuParentId(Integer menuParentId);

    Integer countByMenuParentId(Integer menuParentId);
}
