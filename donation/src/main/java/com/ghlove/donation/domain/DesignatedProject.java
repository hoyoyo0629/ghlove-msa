package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지정기부사업. AS-IS G_DSGN_DNTN_BIZ_MNG - 처음엔 공개화면(목록/상세)에 필요한 일부
 *  컬럼만 매핑했으나(bsnsSubType/contentEtc/deptId/감사필드는 미매핑), admin의 지정기부
 *  관리 라운드에서 실제 쓰기가 필요해져 이미 있던 나머지 컬럼을 마저 매핑했다(새 컬럼
 *  발명 아님). */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_MNG")
@Getter
@Setter
@NoArgsConstructor
public class DesignatedProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DSGN_DNTN_BIZ_ID")
    private Long dsgnDntnBizId;

    @Column(name = "DSGN_DNTN_BIZ_TTL")
    private String dsgnDntnBizTtl;

    @Column(name = "DSGN_DNTN_BIZ_CN")
    private String dsgnDntnBizCn;

    @Column(name = "DSGN_DNTN_BIZ_BGNG_YMD")
    private String dsgnDntnBizBgngYmd;

    @Column(name = "DSGN_DNTN_BIZ_END_YMD")
    private String dsgnDntnBizEndYmd;

    @Column(name = "GOAL_AMT")
    private Long goalAmt;

    @Column(name = "DSGN_DNTN_BIZ_STTS_CD")
    private String dsgnDntnBizSttsCd;

    @Column(name = "RLS_YN")
    private String rlsYn;

    @Column(name = "LCLGV_CD")
    private String lclgvCd;

    /** 사업구분 - 고향사랑 기부금법상 4가지 사업목적(100~400) 중 하나. */
    @Column(name = "DSGN_DNTN_BIZ_SE_CD")
    private String dsgnDntnBizSeCd;

    /** 카드 대표 이미지 - AS-IS 원래 컬럼(DSGN_DNTN_BIZ_RPRS_IMG). AS-IS는 CDN 업로드 파일 경로를
     *  담지만, 이 프로젝트는 이미 static 리소스로 배포된 이미지 경로를 그대로 참조한다. */
    @Column(name = "DSGN_DNTN_BIZ_RPRS_IMG")
    private String imageUrl;

    /** 사업 세부구분 - DSGN_DNTN_BIZ_SE_CD 하위의 더 상세한 분류(캐스케이딩 드롭다운). */
    @Column(name = "DSGN_DNTN_BIZ_SE_DTL_CD")
    private String bsnsSubType;

    /** 기타사항/비고. */
    @Column(name = "DSGN_DNTN_BIZ_ETC_CN")
    private String contentEtc;

    /** 담당부서 - G_DSGN_DNTN_BIZ_DEPT_MNG FK. */
    @Column(name = "DSGN_DNTN_BIZ_DEPT_ID")
    private Long deptId;

    @Column(name = "FRST_RGTR_ID")
    private Long frstRgtrId;

    @Column(name = "LAST_RGTR_ID")
    private Long lastRgtrId;

    @Column(name = "FRST_REG_DT")
    private LocalDateTime frstRegDt;

    @Column(name = "LAST_REG_DT")
    private LocalDateTime lastRegDt;
}
