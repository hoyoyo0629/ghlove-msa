package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지정기부 담당부서 (AS-IS G_DSGN_DNTN_BIZ_DEPT_MNG, opmanager/designated-donation/part).
 *  지자체 소속 부서를 등록해두면 사업 등록 시 담당부서로 지정할 수 있다. */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_DEPT_MNG")
@Getter
@Setter
@NoArgsConstructor
public class DesignatedPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DSGN_DNTN_BIZ_DEPT_ID")
    private Long deptId;

    @Column(name = "DSGN_DNTN_BIZ_DEPT_NM")
    private String deptNm;

    @Column(name = "LCLGV_CD")
    private String locgovCode;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "FRST_RGTR_ID")
    private Long frstRgtrId;

    @Column(name = "LAST_RGTR_ID")
    private Long lastRgtrId;

    @Column(name = "FRST_REG_DT")
    private LocalDateTime frstRegDt;

    @Column(name = "LAST_REG_DT")
    private LocalDateTime lastRegDt;
}
