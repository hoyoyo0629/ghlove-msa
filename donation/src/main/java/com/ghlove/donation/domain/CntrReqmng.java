package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 기부금 변경신청 (AS-IS G_CNTR_REQMNG, opmanager/give/give-reqmng) - 지자체 담당자가
 *  기부건의 취소(과오납)나 포인트 재생성을 요청하면 시스템/행안부 담당자가 승인한다.
 *  REQ_STATUS_CODE: 100=요청(대기) 200=취소 999=승인 (기존 DDL 주석과 동일). */
@Entity
@Table(name = "G_CNTR_REQMNG")
@Getter
@Setter
@NoArgsConstructor
public class CntrReqmng {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cntrReqmngReqIdSeq")
    @SequenceGenerator(name = "cntrReqmngReqIdSeq", sequenceName = "g_cntr_reqmng_req_id_seq", allocationSize = 1)
    @Column(name = "REQ_ID")
    private Long reqId;

    @Column(name = "LOGIN_ID")
    private String loginId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "STTEMNT_PAY_DE")
    private LocalDateTime sttemntPayDe;

    @Column(name = "CNTR_AMT")
    private BigDecimal cntrAmt;

    /** 100: 기부금 취소(과오납), 200: 포인트 생성. */
    @Column(name = "CNTR_REQMNG_CODE")
    private String cntrReqmngCode;

    @Column(name = "DISCRIPTION")
    private String discription;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "REQ_STATUS_CODE")
    private String reqStatusCode;

    @Column(name = "CNTR_SN")
    private String cntrSn;

    @Column(name = "TAX_SYS_CANCEL_DE")
    private LocalDateTime taxSysCancelDe;

    @Column(name = "RELATED_DOC_DPT_NM")
    private String relatedDocDptNm;

    @Column(name = "RELATED_DOC_NUM")
    private String relatedDocNum;

    @Column(name = "RELATED_DOC_DE")
    private LocalDateTime relatedDocDe;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    @Column(name = "APPR_DT")
    private LocalDateTime apprDt;

    @Column(name = "CANCLE_DT")
    private LocalDateTime cancleDt;
}
