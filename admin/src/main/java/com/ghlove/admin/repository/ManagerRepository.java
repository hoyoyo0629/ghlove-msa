package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    Optional<Manager> findByLoginId(String loginId);

    Optional<Manager> findByCertSubjectDn(String certSubjectDn);

    /** D2 매니저계정관리 목록용 (AS-IS UserManagerController manager/list). */
    List<Manager> findAllByOrderByUserIdDesc();

    /** 권한그룹 목록의 "인원"(M01694) - AS-IS는 OP_USER_ROLE을 LEFT JOIN해 count(USER_ID)로 센다.
     *  TO-BE는 매니저가 AUTHORITY를 직접 들고 있어(OP_MANAGER.AUTHORITY) 그 역할을 가진 매니저 수다. */
    long countByAuthority(String authority);
}
