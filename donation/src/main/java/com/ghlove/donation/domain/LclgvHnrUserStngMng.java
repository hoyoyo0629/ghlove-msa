package com.ghlove.donation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 지자체별 기부혜택증 설정 - AS-IS {@code G_LCLGV_HNR_USER_STNG_MNG} 재현
 * (운영관리 메뉴 19101 기부혜택증 설정 관리 / 19102 기부혜택증 설정).
 *
 * <p>기부혜택증(AS-IS 명칭 "명예시도민증")은 일정 금액 이상 기부한 사람에게 지자체가 발급하는
 * 전자 증서다. 이 표가 지자체별로 <b>발급기준금액·발급기준(선정구분)·명칭·발급자 혜택 안내·
 * 대표이미지·사용여부</b>를 들고 있다.
 *
 * <p><b>왜 donation에 있는가</b>: 기부혜택증은 기부실적으로 발급되는 기부 도메인 자산이고,
 * 형제 표({@code G_HONOR_BENEFIT}·{@code G_HONOR_CNTRBTR}·{@code G_HONOR_CNTRBTR_STDR}·
 * 열람이력 {@code OP_HONOR_VIEW_HIST}·보상이미지 설명 {@code G_LCLGV_HNR_USER_RWRD_IMG_EXPLN})이
 * 모두 donation 스키마에 있다. admin 스키마에도 같은 이름의 빈 표가 있었는데(예전 TO-BE 구현이
 * 만든 것) 실제 데이터와 형제 표가 donation에 있으므로 이쪽을 정본으로 쓴다 - admin 화면은
 * 이 서비스의 API를 호출한다.
 *
 * <p>AS-IS 골드·실버 등급 금액({@code GLD_GRD_DNTN_AMT}/{@code SLVR_GRD_DNTN_AMT})은 화면에서
 * <b>주석처리</b>되어 있고 발급기준금액으로 쓰이는 것은 {@code BRNZ_GRD_DNTN_AMT} 하나다.
 * 컬럼은 그대로 두고(비활성 상태 보존) 화면도 AS-IS처럼 브론즈만 노출한다.
 */
@Entity
@Table(name = "g_lclgv_hnr_user_stng_mng")
@Getter
@Setter
@NoArgsConstructor
public class LclgvHnrUserStngMng {

    /** 지자체 코드(G_LOCGOV.LOCGOV_CODE). */
    @Id
    @Column(name = "LCLGV_CD", length = 10)
    private String lclgvCd;

    /** 골드 등급 기부금액 - AS-IS 화면에서 주석처리된 항목. */
    @Column(name = "GLD_GRD_DNTN_AMT")
    private Integer gldGrdDntnAmt;

    /** 실버 등급 기부금액 - AS-IS 화면에서 주석처리된 항목. */
    @Column(name = "SLVR_GRD_DNTN_AMT")
    private Integer slvrGrdDntnAmt;

    /** 발급기준금액 - AS-IS 화면의 유일한 금액 입력칸이다("원 이상"). */
    @Column(name = "BRNZ_GRD_DNTN_AMT")
    private Integer brnzGrdDntnAmt;

    /** 기부혜택증 명칭(권장 8자). */
    @Column(name = "HNR_USER_STNG_TTL")
    private String hnrUserStngTtl;

    /** 기부혜택증 발급자 혜택 안내 - 에디터로 입력하는 HTML이다. */
    @Column(name = "HNR_USER_RWRD")
    private String hnrUserRwrd;

    /** 대표(메인) 이미지 파일명. */
    @Column(name = "RPRS_IMG_NM")
    private String rprsImgNm;

    /** 발급기준 구분코드 - 공통코드 HNR_USER_SLCTN_SE_CD(기준1 전년도 / 기준2 최근1년 / 기준3 회계연도). */
    @Column(name = "HNR_USER_SLCTN_SE_CD")
    private String hnrUserSlctnSeCd;

    @Column(name = "USE_YN", length = 1)
    private String useYn;

    @Column(name = "FRST_RGTR_ID")
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT")
    private LocalDateTime frstRegDt;

    @Column(name = "LAST_RGTR_ID")
    private Long lastRgtrId;

    @Column(name = "LAST_REG_DT")
    private LocalDateTime lastRegDt;
}
