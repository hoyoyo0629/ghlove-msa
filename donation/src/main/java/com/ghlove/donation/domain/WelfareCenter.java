package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 행정복지센터(주민센터, 오프라인 기부접수 거점) 관리 - AS-IS opmanager/welfareCenter -
 * WelfareCenterManagerController. Maps G_LCLGV_PBADMS_WLFR_CNTR_MNG. */
@Entity
@Table(name = "G_LCLGV_PBADMS_WLFR_CNTR_MNG")
@Getter
@Setter
@NoArgsConstructor
public class WelfareCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PBADMS_WLFR_CNTR_ID")
    private Long pbadmsWlfrCntrId;

    @Column(name = "LCLGV_CD")
    private String lclgvCd;

    @Column(name = "PBADMS_WLFR_CNTR_NM")
    private String pbadmsWlfrCntrNm;

    @Column(name = "PBADMS_WLFR_CNTR_CD")
    private String pbadmsWlfrCntrCd;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "FRST_REG_DT")
    private LocalDateTime frstRegDt;
}
