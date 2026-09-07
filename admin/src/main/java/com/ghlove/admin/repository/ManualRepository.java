package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Manual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualRepository extends JpaRepository<Manual, Long> {
    List<Manual> findAllByOrderByMnlSnDesc();
}
