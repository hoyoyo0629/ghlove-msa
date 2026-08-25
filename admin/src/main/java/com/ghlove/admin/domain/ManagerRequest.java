package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 권한 요청 (AS-IS G_MNGR_REQST) - 이미 회원가입돼 있는 사람(member 서비스의
 * USER_ID)이 운영관리 콘솔 접근 권한을 요청하는 신청서. 승인되면 OP_MANAGER 행이 새로
 * 생성된다. USER_ID당 REQST_SN이 순차 증가하는 자연복합키(재신청 이력이 쌓이는 구조).
 */
@Entity
@Table(name = "G_MNGR_REQST")
@IdClass(ManagerRequestId.class)
@Getter
@Setter
@NoArgsConstructor
public class ManagerRequest {

    public static final String STATUS_PENDING = "200";
    public static final String STATUS_APPROVED = "100";
    public static final String STATUS_REJECTED = "300";

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "REQST_SN")
    private Integer reqstSn;

    @Column(name = "LOGIN_ID")
    private String loginId;

    /** 신청자가 담당할 광역지자체 코드 (G_LOCGOV.UPPER_LOCGOV_CODE 참조, cross-service). */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    /** 소속구분 (ADMIN_COMMON_CODE REQST_SE_CODE). */
    @Column(name = "REQST_SE_CODE")
    private String reqstSeCode;

    @Column(name = "PSITN_NM")
    private String psitnNm;

    @Column(name = "PSITN_DEPT_NM")
    private String psitnDeptNm;

    @Column(name = "OFCPS_NM")
    private String ofcpsNm;

    @Column(name = "CTTPC")
    private String cttpc;

    /** 100=승인, 200=대기, 300=거절. */
    @Column(name = "CONFM_STTUS_CODE")
    private String confmSttusCode;

    @Column(name = "REJECT_RESN")
    private String rejectResn;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private String frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private String lastUpdtPnttm;
}
