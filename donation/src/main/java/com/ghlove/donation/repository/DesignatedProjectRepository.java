package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignatedProjectRepository extends JpaRepository<DesignatedProject, Long> {
    List<DesignatedProject> findByRlsYnAndDsgnDntnBizSttsCdOrderByDsgnDntnBizIdDesc(String rlsYn, String sttsCd);
}
