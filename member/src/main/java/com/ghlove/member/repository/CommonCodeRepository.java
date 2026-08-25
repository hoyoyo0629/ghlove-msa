package com.ghlove.member.repository;

import com.ghlove.member.domain.CommonCode;
import com.ghlove.member.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    List<CommonCode> findByCodeTypeAndLanguageAndUseYnOrderByOrdering(String codeType, String language, String useYn);
}
