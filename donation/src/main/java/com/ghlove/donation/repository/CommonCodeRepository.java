package com.ghlove.donation.repository;

import com.ghlove.donation.domain.CommonCode;
import com.ghlove.donation.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    List<CommonCode> findByCodeTypeAndLanguageAndUseYnOrderByOrdering(String codeType, String language, String useYn);
}
