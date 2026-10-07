package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공통코드 (SFR-007 "공통 코드·환경설정 관리" - 하드코딩 금지 원칙의 실제 관리 지점).
 * AS-IS 테이블은 {@code OP_COMMON_CODE} 하나뿐이다(code-mapper.xml 등 41개 매퍼가 참조) -
 * {@code ADMIN_COMMON_CODE}라는 이름은 AS-IS에 존재하지 않는다. 예전에 이 엔티티가
 * ADMIN_COMMON_CODE(TO-BE가 지은 이름)를 가리키는 바람에 실데이터가 들어있는 OP_COMMON_CODE
 * (2026-10-02 AS-IS export 적재)는 아무도 안 읽는 고아 테이블이 되어 있었고, 화면이 읽던
 * ADMIN_COMMON_CODE는 DETAIL 등 일부 컬럼이 부실했다("코드상세가 AS-IS와 다르다" 원인,
 * 2026-10-07 교정). ADMIN_COMMON_CODE에만 있던 TO-BE 전용 의미코드 7종(BSNS_PURPS_CODE 등)은
 * migration-admin-common-code-retire-wrong-table.sql로 OP_COMMON_CODE에 옮겨 담았다.
 * Round 1 scope: admin manages its own OP_COMMON_CODE only (DB-per-service means it
 * can't reach into member/donation/point/gift/order's own copies directly).
 */
@Entity
@Table(name = "OP_COMMON_CODE")
@IdClass(CommonCodeId.class)
@Getter
@Setter
@NoArgsConstructor
public class CommonCode {

    @Id
    @Column(name = "CODE_TYPE")
    private String codeType;

    @Id
    @Column(name = "LANGUAGE")
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

    /** AS-IS EXTENSION_CODE - 등록/수정 폼의 "확장코드"(M01684). AS-IS 도메인 필드명은
     *  extentionCode지만(오타) 컬럼은 EXTENSION_CODE다. */
    @Column(name = "EXTENSION_CODE")
    private String extensionCode;

    /** AS-IS MAPPING_CODE - 등록/수정 폼의 "매핑코드"(M01685). */
    @Column(name = "MAPPING_CODE")
    private String mappingCode;
}
