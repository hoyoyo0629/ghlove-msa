package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 권한 라벨 (AS-IS OP_ROLE) - AS-IS 개발DB에는 <b>ROLE_ADMIN_1~10 열 행</b>이 있다
 *  (시스템·행안부·지자체·오프라인 4그룹 × 정·부담당자 8 + 9 답례품관리자 + 10 지정기부사업자).
 *  ROLE_EXCEL·ROLE_ISMS·ROLE_MD는 목록 조회 조건에는 들어 있지만 AS-IS OP_ROLE에 행이 없다.
 *  {@code role_seq}는 AS-IS에 없는 TO-BE 전용 컬럼이다(정렬 동순위 안정화용). */
@Entity
@Table(name = "OP_ROLE")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @Column(name = "AUTHORITY")
    private String authority;

    @Column(name = "ROLE_NAME")
    private String roleName;

    @Column(name = "ROLE_DESC")
    private String roleDesc;

    @Column(name = "ROLE_SEQ")
    private Integer roleSeq;

    /** AS-IS는 생성일을 목록의 "생성일자"(M01692) 컬럼에 보여주고 정렬키로도 쓴다
     *  (getUserGroupList의 ORDER BY opr.CREATED_DATE DESC). AS-IS 저장 형식은
     *  CommonMapper.datetime = yyyyMMddHHmmss 문자열이다. */
    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "CREATED_USER_ID")
    private String createdUserId;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;

    @Column(name = "UPDATED_USER_ID")
    private String updatedUserId;
}
