package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 지자체별 연간 기부한도/포인트적립률 설정 (G_CTBNY_SETUP). Per-locgov and per-year,
 * so it must be looked up here rather than hardcoded (no-hardcoding principle). AS-IS의
 * user/locgov 화면이 부르던 ContributionSetup 도메인/"포인트 지급률" 팝업이 바로 이 테이블이다 -
 * admin 지자체관리 화면의 포인트 지급률 등록/이력조회가 이 엔티티를 그대로 쓴다(연도별 행 자체가
 * 이력이라 별도 이력 테이블이 필요 없다).
 */
@Entity
@Table(name = "G_CTBNY_SETUP")
@IdClass(CtbnySetupId.class)
@Getter
@Setter
@NoArgsConstructor
public class CtbnySetup {

    @Id
    @Column(name = "STDR_YEAR")
    private String stdrYear;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "LMT_AMT")
    private Integer lmtAmt;

    /** 포인트 지급률 (%). */
    @Column(name = "POINT_RATE")
    private BigDecimal pointRate;

    @Column(name = "POINT_VALID_PD")
    private Integer pointValidPd;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    /** admin 매니저 정보는 별도 서비스라 FK 없이 쓰기 시점 이름을 그대로 저장(이력 표시용). */
    @Column(name = "LAST_UPDUSR_NM")
    private String lastUpdusrNm;
}
