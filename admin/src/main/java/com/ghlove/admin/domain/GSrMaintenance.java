package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 운영유지관리 SR게시판 (AS-IS opmanager/maintenance - MaintenanceController, G_SR_MAINTENANCE).
 * 시스템/행안부 담당자가 운영 중 발생한 요청(SR)을 접수하고 처리 이력을 남기는 내부용
 * 게시판이다. 완료구분(processState)/KI구분/요청경로/업무구분은 AS-IS가 공통코드
 * (MAINTEN_STATE_CODE 등)로 관리했지만 이 프로젝트 공통코드 테이블에는 해당 그룹이
 * 시딩되어 있지 않아, 화면에서는 AS-IS 코드 값 그대로(접수/처리중/처리완료/제외 등)
 * 정적 옵션으로 노출한다. */
@Entity
@Table(name = "G_SR_MAINTENANCE")
@Getter
@Setter
@NoArgsConstructor
public class GSrMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gSrMaintenanceBbsIdSeq")
    @SequenceGenerator(name = "gSrMaintenanceBbsIdSeq", sequenceName = "g_sr_maintenance_bbs_id_seq", allocationSize = 1)
    @Column(name = "BBS_ID")
    private Long bbsId;

    @Column(name = "SR_NO")
    private String srNo;

    /** 1: 홈페이지, 2: 관리자시스템. */
    @Column(name = "KI_TYPE")
    private String kiType;

    /** yyyyMMdd - 요청 접수일. */
    @Column(name = "REQ_CRT_DATE")
    private String reqCrtDate;

    @Column(name = "REQ_DT")
    private LocalDateTime reqDt;

    @Column(name = "REQ_USER_PHONE_NUMBER")
    private String reqUserPhoneNumber;

    /** 요청경로: 소통방/회의/이메일/SR게시판/내부메신저/SNS/전화/KLID/사업장/기타. */
    @Column(name = "REQ_CHANNEL")
    private String reqChannel;

    @Column(name = "REQ_TYPE")
    private String reqType;

    /** 업무구분: 기부/답례품/기타. */
    @Column(name = "PROCESS_TYPE")
    private String processType;

    @Column(name = "BBS_TTL")
    private String bbsTtl;

    @Column(name = "BBS_CN")
    private String bbsCn;

    @Column(name = "REQ_USER_INFO")
    private String reqUserInfo;

    @Column(name = "URGENT_YN")
    private String urgentYn;

    @Column(name = "REQ_SOURCE")
    private String reqSource;

    @Column(name = "PROCESS_MANAGER_ID")
    private String processManagerId;

    @Column(name = "PROCESS_MANAGER_NM")
    private String processManagerNm;

    @Column(name = "PROCESS_CN")
    private String processCn;

    @Column(name = "PROCESS_RECEIPT_DATE")
    private String processReceiptDate;

    @Column(name = "PROCESS_TARGET_END_DATE")
    private String processTargetEndDate;

    @Column(name = "PROCESS_START_DATE")
    private String processStartDate;

    @Column(name = "PROCESS_END_DATE")
    private String processEndDate;

    /** 완료구분: 접수/처리중/처리완료/제외. */
    @Column(name = "PROCESS_STATE")
    private String processState;

    @Column(name = "DEPLOY_DATE")
    private String deployDate;

    @Column(name = "RM")
    private String rm;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "FRST_CRT_DT")
    private LocalDateTime frstCrtDt;

    @Column(name = "FRST_CRT_ID")
    private Long frstCrtId;

    @Column(name = "LAST_MDFCN_ID")
    private Long lastMdfcnId;

    @Column(name = "LAST_MDFCN_DT")
    private LocalDateTime lastMdfcnDt;

    @Column(name = "INQ_CNT")
    private Long inqCnt;
}
