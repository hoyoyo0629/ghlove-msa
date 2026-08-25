package com.ghlove.order.repository;

import com.ghlove.order.domain.CommonCode;
import com.ghlove.order.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    List<CommonCode> findByCodeTypeAndLanguageAndUseYnOrderByOrdering(String codeType, String language, String useYn);
}
