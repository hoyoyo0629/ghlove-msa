package com.ghlove.admin.repository;

import com.ghlove.admin.domain.MenuRight;
import com.ghlove.admin.domain.MenuRightId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRightRepository extends JpaRepository<MenuRight, MenuRightId> {

    List<MenuRight> findByMenuId(Integer menuId);

    boolean existsByMenuIdAndAuthority(Integer menuId, String authority);
}
