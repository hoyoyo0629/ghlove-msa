package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Local government registry (지자체). Maps a subset of the AS-IS G_LOCGOV columns. */
@Entity
@Table(name = "G_LOCGOV")
@Getter
@Setter
@NoArgsConstructor
public class Locgov {

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "LOCGOV_NM")
    private String locgovNm;

    @Column(name = "UPPER_LOCGOV_NM")
    private String upperLocgovNm;

    @Column(name = "UPPER_LOCGOV_CODE")
    private String upperLocgovCode;

    /** 지자체 사업자등록번호 - 기부확인증의 "(사업자번호 : ...)" 표기에 사용. */
    @Column(name = "BIZRNO")
    private String bizrno;

    @Column(name = "USE_AT")
    private String useAt;

    /** 명예기부자(기부혜택증) 등급 구간 - 해당 연도 누적 기부액이 이 값을 넘으면 그 등급을 받는다. */
    @Column(name = "STDR_1LEVEL_AMT")
    private Integer stdr1LevelAmt;

    @Column(name = "STDR_2LEVEL_AMT")
    private Integer stdr2LevelAmt;

    @Column(name = "STDR_3LEVEL_AMT")
    private Integer stdr3LevelAmt;

    /** 기금사업소개(list-select) 화면의 "지자체정보" 탭에서 쓰는 실 레거시 컬럼들 - 이번
     * 라운드까지 매핑되지 않고 있었다. */
    @Column(name = "LOCGOV_INTRCN_CN")
    private String locgovIntrcnCn;

    @Column(name = "CHARGER_NM")
    private String chargerNm;

    @Column(name = "CHARGER_CTTPC")
    private String chargerCttpc;

    @Column(name = "CHARGER_PSITN_DEPT")
    private String chargerPsitnDept;

    @Column(name = "LOCGOV_HMPG")
    private String locgovHmpg;

    @Column(name = "LOCGOV_POPLTN_CO")
    private String locgovPopltnCo;

    @Column(name = "LOCGOV_BUDGET_AMT")
    private Long locgovBudgetAmt;
}
