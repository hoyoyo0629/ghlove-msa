package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 기부금 지출내역 (AS-IS G_CTBNY_OPRATN, opmanager/give/give-operation) - 지자체 담당자가
 * 고향사랑기부금 사용 내역을 사업 단위로 등록한다(고향사랑 기부금법상 사용내역 공개 의무).
 * 배치 스캔으로 이미 있던 테이블이라 REGIST_SN에 시퀀스만 보강했다.
 */
@Entity
@Table(name = "G_CTBNY_OPRATN")
@Getter
@Setter
@NoArgsConstructor
public class CtbnyOpratn {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ctbnyOpratnRegistSnSeq")
    @SequenceGenerator(name = "ctbnyOpratnRegistSnSeq", sequenceName = "g_ctbny_opratn_regist_sn_seq", allocationSize = 1)
    @Column(name = "REGIST_SN")
    private Long registSn;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    /** ADMIN_COMMON_CODE 아니라 donation 자체 OP_COMMON_CODE의 CNTR_USE_PURPS 참조. */
    @Column(name = "BSNS_PURPS_CODE")
    private String bsnsPurpsCode;

    @Column(name = "BSNS_NM")
    private String bsnsNm;

    @Column(name = "BSNS_CN")
    private String bsnsCn;

    // DB 컬럼이 VARCHAR(8) 지출일자(yyyyMMdd) 라서 LocalDateTime으로 매핑하면 조회 자체가
    // 깨진다(Bad value for type timestamp). 다른 날짜성 컬럼들처럼 yyyyMMdd 문자열로 다룬다.
    @Column(name = "EXPNDTR_DE")
    private String expndtrDe;

    @Column(name = "EXPNDTR_AMT")
    private BigDecimal expndtrAmt;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    @Column(name = "RM")
    private String rm;
}
