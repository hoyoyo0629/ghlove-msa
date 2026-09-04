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
}
