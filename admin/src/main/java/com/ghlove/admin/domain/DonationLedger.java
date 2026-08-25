package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 통계용 ReadModel - donation.lifecycle 이벤트를 구독해 쌓는 로컬 사본
 * (admin은 donation 서비스의 DB를 직접 읽을 수 없음, DB per Service).
 */
@Entity
@Table(name = "STAT_DONATION_LEDGER")
@Getter
@Setter
@NoArgsConstructor
public class DonationLedger {

    @Id
    @Column(name = "CNTR_SN")
    private String cntrSn;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "EVENT_DATE")
    private String eventDate;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    /** report/generalTotal "기부방법별 기부 현황" 탭용 (donation.CntrPathCode 그대로). */
    @Column(name = "CNTR_PATH_CODE")
    private String cntrPathCode;

    /** report/generalTotal "거주지역->기부지역 건수 현황" 탭용 (donation.PsitnLocgovCode 그대로 -
     *  고향사랑기부금법상 본인 주소지 지자체에는 기부 불가라 cntrLocgovCode와 항상 다름). */
    @Column(name = "PSITN_LOCGOV_CODE")
    private String psitnLocgovCode;
}
