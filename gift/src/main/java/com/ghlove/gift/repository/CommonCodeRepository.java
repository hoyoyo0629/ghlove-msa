package com.ghlove.gift.repository;

import com.ghlove.gift.domain.CommonCode;
import com.ghlove.gift.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    List<CommonCode> findByCodeTypeAndLanguageAndUseYnOrderByOrdering(String codeType, String language, String useYn);

    List<CommonCode> findByCodeTypeAndLanguageOrderByOrdering(String codeType, String language);
}
