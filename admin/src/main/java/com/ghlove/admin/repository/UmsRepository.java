package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Ums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UmsRepository extends JpaRepository<Ums, Long> {
    List<Ums> findAllByOrderByIdDesc();

    boolean existsByTemplateCode(String templateCode);
}
