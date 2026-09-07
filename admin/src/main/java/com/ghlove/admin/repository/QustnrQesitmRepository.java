package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QustnrQesitm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QustnrQesitmRepository extends JpaRepository<QustnrQesitm, Long> {
    List<QustnrQesitm> findByQustnrSnOrderByQestnSeq(Long qustnrSn);

    void deleteByQustnrSn(Long qustnrSn);
}
