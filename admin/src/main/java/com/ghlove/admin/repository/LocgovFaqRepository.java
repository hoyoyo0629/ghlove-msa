package com.ghlove.admin.repository;

import com.ghlove.admin.domain.LocgovFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocgovFaqRepository extends JpaRepository<LocgovFaq, Integer> {
    List<LocgovFaq> findByUseYn(String useYn);

    List<LocgovFaq> findAllByOrderByIdDesc();
}
