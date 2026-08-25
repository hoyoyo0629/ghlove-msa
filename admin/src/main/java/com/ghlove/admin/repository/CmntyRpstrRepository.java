package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyRpstr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmntyRpstrRepository extends JpaRepository<CmntyRpstr, Long> {
    List<CmntyRpstr> findAllByOrderByRpstrIdDesc();
}
