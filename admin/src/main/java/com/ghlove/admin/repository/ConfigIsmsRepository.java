package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ConfigIsms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfigIsmsRepository extends JpaRepository<ConfigIsms, String> {

    /**
     * AS-IS config-isms-mapper.xml getIsmsList: {@code ORDER BY ISMS_TYPE, ORDERING}. 구분(공통/
     * 관리자/회원)별로 먼저 묶어야 ConfigIsmsController의 rowspan 묶음 로직(같은 타입이 연속해
     * 나온다고 가정)이 성립한다 - ORDERING만으로 정렬하면(이전 TO-BE 결함) 실제 데이터에서
     * ORDERING 값이 타입을 가로질러 섞여 있어(회원 13이 공통 11·14 사이에 끼는 등) 묶음이 깨진다.
     */
    List<ConfigIsms> findAllByOrderByIsmsTypeAscOrderingAsc();
}
