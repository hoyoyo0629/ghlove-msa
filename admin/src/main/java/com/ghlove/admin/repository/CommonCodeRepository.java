package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    List<CommonCode> findByCodeTypeOrderByOrdering(String codeType);

    List<CommonCode> findAllByOrderByCodeTypeAscOrderingAsc();
}
