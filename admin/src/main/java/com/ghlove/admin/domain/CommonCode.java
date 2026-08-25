package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공통코드 (SFR-007 "공통 코드·환경설정 관리" - 하드코딩 금지 원칙의 실제 관리 지점).
 * Round 1 scope: admin manages its own ADMIN_COMMON_CODE only (DB-per-service
 * means it can't reach into member/donation/point/gift/order's own copies
 * directly) - see the DDL migration comment for how a cross-service version
 * would extend this later.
 */
@Entity
@Table(name = "ADMIN_COMMON_CODE")
@IdClass(CommonCodeId.class)
@Getter
@Setter
@NoArgsConstructor
public class CommonCode {

    @Id
    @Column(name = "CODE_TYPE")
    private String codeType;

    @Id
    @Column(name = "CODE_LANGUAGE")
    private String language;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "LABEL")
    private String label;

    @Column(name = "DETAIL")
    private String detail;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "UP_ID")
    private String upId;

    @Column(name = "CODE_VALUE")
    private String codeValue;
}
