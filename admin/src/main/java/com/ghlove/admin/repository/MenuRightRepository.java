package com.ghlove.admin.repository;

import com.ghlove.admin.domain.MenuRight;
import com.ghlove.admin.domain.MenuRightId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRightRepository extends JpaRepository<MenuRight, MenuRightId> {

    List<MenuRight> findByMenuId(Integer menuId);

    boolean existsByMenuIdAndAuthority(Integer menuId, String authority);

    /** D10 역할·메뉴권한 매트릭스 화면 - 특정 역할이 현재 가진 메뉴권한 전체. */
    List<MenuRight> findByAuthority(String authority);
}
