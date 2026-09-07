package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 사용자매뉴얼 관리 (AS-IS opmanager/manual - ManualManagerController, 신규 테이블).
 * AS-IS는 사용자매뉴얼(manual/user/*)과 관리자메뉴 도움말(manual/manager/*, OP_MENU 1:1 연동)
 * 2개 화면으로 나뉘지만, 이 프로젝트 지시서 범위는 사용자매뉴얼 화면만이다. AS-IS 원본
 * DB에는 이 테이블이 없었다(신규 설계) - 지시서에 명시된 컬럼 그대로 구성했다. */
@Entity
@Table(name = "OP_MANUAL")
@Getter
@Setter
@NoArgsConstructor
public class Manual {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opManualMnlSnSeq")
    @SequenceGenerator(name = "opManualMnlSnSeq", sequenceName = "op_manual_mnl_sn_seq", allocationSize = 1)
    @Column(name = "MNL_SN")
    private Long mnlSn;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT")
    private String content;

    /** 이 매뉴얼이 대응하는 화면(메뉴) 식별 코드 - 자유 텍스트(예: "/admin/maintenance"). */
    @Column(name = "MENU_URL_CODE")
    private String menuUrlCode;

    @Column(name = "FILE_SRC")
    private String fileSrc;

    @Column(name = "ORGNL_FILE_NM")
    private String orgnlFileNm;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "USE_YN")
    private String useYn;
}
