package com.ghlove.admin.repository;

import com.ghlove.admin.domain.LclgvHnrUserStngMng;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LclgvHnrUserStngMngRepository extends JpaRepository<LclgvHnrUserStngMng, String> {
    List<LclgvHnrUserStngMng> findAllByOrderByLclgvCd();
}
