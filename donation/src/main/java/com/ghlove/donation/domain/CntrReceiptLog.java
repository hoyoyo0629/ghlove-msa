package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 기부금영수증(단건 공식 영수증) 출력 이력 - AS-IS G_CNTR_RCIPT. AS-IS는 OZReport
 * 뷰어에서 실제 인쇄가 완료된 시점(OZPrintCommand_OZViewer 콜백)에만 한 행을 남기고,
 * 재출력할 때마다 CNTR_OUTPT_SN(전역 순번, MAX+1)이 새로 발급된다 - 같은 기부건이라도
 * 여러 번 출력하면 여러 행이 쌓인다.
 */
@Entity
@Table(name = "G_CNTR_RCIPT")
@Getter
@Setter
@NoArgsConstructor
public class CntrReceiptLog {

    @Id
    @Column(name = "CNTR_OUTPT_SN")
    private Integer cntrOutptSn;

    @Column(name = "CNTR_SN")
    private String cntrSn;

    /** AS-IS ELCTRN_PAY_NO(전자납부번호) - 이 프로젝트에서는 DonationLevy.bugaNo(세외수입 부과번호)가
     * 그 역할을 한다(같은 세외수입 연계 흐름에서 나온 값). */
    @Column(name = "ELCTRN_PAY_NO")
    private String elctrnPayNo;

    /** yyyyMMdd - 발급일자. */
    @Column(name = "ISSU_DE")
    private String issuDe;

    @Column(name = "ISSU_CO")
    private Integer issuCo;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;
}
