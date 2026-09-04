package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Local government registry (지자체). AS-IS G_LOCGOV - now maps every column (as of the
 * admin 지자체 마스터관리 gap-fill round; previously only a subset used by other screens
 * was mapped). See database/ddl/service-donation.sql G_LOCGOV for the authoritative column
 * list this mirrors. */
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

    /** 지자체 사업자등록번호 - 기부확인증의 "(사업자번호 : ...)" 표기에 쓰고, admin 등록화면에서는
     * 이 값이 채워져 있는지를 "등록 완료" 여부의 기준으로 삼는다(AS-IS validator의 첫 필수값). */
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

    /** 직인명 - AS-IS opmanager/user/locgov/edit.jsp의 "직인명" 입력값. */
    @Column(name = "OFFCS_NM")
    private String offcsNm;

    /** 암호화된 직인 이미지 파일이 저장된 파일명(디스크상의 실제 저장 파일명, 원본명 아님). */
    @Column(name = "OFFCS_FILE_NM")
    private String offcsFileNm;

    /** 업로드 당시의 원본 파일명 - MIME 타입 추정에 사용. */
    @Column(name = "ORGINL_FILE_NM")
    private String orginlFileNm;

    /** 지자체 면적. */
    @Column(name = "LOCGOV_AR")
    private String locgovAr;

    /** 지자체 특산물. */
    @Column(name = "LOCGOV_SPCPRD")
    private String locgovSpcprd;

    /** 상품권 사용 여부 (Y/N). */
    @Column(name = "GCCT_USE_AT")
    private String gcctUseAt;

    /** 전자화폐 사용 여부 (Y/N). */
    @Column(name = "ETRCSH_USE_AT")
    private String etrcshUseAt;

    @Column(name = "LOCGOV_ZIP")
    private String locgovZip;

    /** 기본 주소. */
    @Column(name = "BASS_ADRES")
    private String bassAdres;

    /** 상세 주소. */
    @Column(name = "DTL_ADRES")
    private String dtlAdres;

    /** 주류 판매 여부 (Y/N). */
    @Column(name = "ACHLQR_SLE_AT")
    private String achlqrSleAt;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    /** 처리부서코드 - 11자리(AS-IS validator). 변경 이력은 {@link LocgovDeptHist}에 별도로 남는다. */
    @Column(name = "PROCESS_DEPT_CODE")
    private String processDeptCode;

    /** 행정표준기관코드 - 7자리. */
    @Column(name = "ADMINIST_INSTT_CODE")
    private String administInsttCode;

    /** 회계구분 - 광역(코드끝 000)이면 31(일반회계)/51(특별회계), 기초지자체면 41/61. */
    @Column(name = "FIS_SP")
    private String fisSp;

    @Column(name = "CHARGER_EMAIL")
    private String chargerEmail;

    @Column(name = "ORDERING")
    private Long ordering;
}
