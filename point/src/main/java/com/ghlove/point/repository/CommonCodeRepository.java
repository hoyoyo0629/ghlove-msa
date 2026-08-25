package com.ghlove.point.repository;

import com.ghlove.point.domain.CommonCode;
import com.ghlove.point.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    List<CommonCode> findByCodeTypeAndLanguageAndUseYnOrderByOrdering(String codeType, String language, String useYn);
}
