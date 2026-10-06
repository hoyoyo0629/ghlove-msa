package com.ghlove.admin.repository;

import com.ghlove.admin.domain.MenuSeller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuSellerRepository extends JpaRepository<MenuSeller, Integer> {

    /** 삭제 전 하위메뉴 검사 (AS-IS는 목록 JS의 menuChild로만 막는다). */
    int countByMenuParentId(Integer menuParentId);
}
