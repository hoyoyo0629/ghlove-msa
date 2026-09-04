package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지자체 기부금모금 제한 (G_CNTR_LMTT) - admin 지자체관리 등록/수정 화면의 "기부금모금제한
 * (기간+사유)" 섹션. 위반 사유로 특정 기간 동안 그 지자체로의 기부를 막는 레코드로, PK가
 * 기간+지자체 자연키라 수정 없이 등록/삭제만 한다(AS-IS와 동일 - 이미 있던 실제 DDL 테이블이지만
 * 이번 라운드까지 TO-BE 어디에서도 쓰이지 않았다). */
@Entity
@Table(name = "G_CNTR_LMTT")
@IdClass(LocgovLmttId.class)
@Getter
@Setter
@NoArgsConstructor
public class LocgovLmtt {

    /** yyyyMMdd. */
    @Id
    @Column(name = "LMTT_BGN_DE")
    private String lmttBgnDe;

    /** yyyyMMdd. */
    @Id
    @Column(name = "LMTT_END_DE")
    private String lmttEndDe;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "VIOLT_RESN_CODE")
    private String violtResnCode;

    @Column(name = "VIOLT_RESN_CN")
    private String violtResnCn;

    @Column(name = "REGISTER_NM")
    private String registerNm;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;
}
