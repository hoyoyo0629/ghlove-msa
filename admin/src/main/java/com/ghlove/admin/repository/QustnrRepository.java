package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Qustnr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QustnrRepository extends JpaRepository<Qustnr, Long> {
    List<Qustnr> findAllByOrderByQustnrSnDesc();
}
