package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 개인정보 접근로그 (AS-IS OP_PRIVACY_ACCESS_LOG) - 엑셀다운로드 사유 관리(1411) 화면이 읽는 표이고,
 * 엑셀 다운로드 사유를 받는 공통 엔드포인트가 쓰는 표다.
 *
 * AS-IS 화면 컬럼과의 대응: 등록일(createdAt) · 관리자ID(managerId로 OP_MANAGER.LOGIN_ID를 찾아 표시)
 * · 다운로드 메뉴(name) · URL(url) · 사유(reason) · 사유구분(reasonType → EXCELDOWNLOAD_REASON_TYPE 라벨).
 * 사유를 고치면 변경이력이 OP_PRIVACY_ACCESS_LOG_HIST에 쌓이고, 2건 이상이면 "이력보기"가 뜬다.
 *
 * ID는 AS-IS가 시퀀스({@code OP_PRIVACY_ACCESS_LOG_SEQ})로 채번하는데 TO-BE DB에는 그 시퀀스가
 * 없어서 {@code migration-admin-privacy-access-log-seq.sql}로 만들었다.
 */
@Entity
@Table(name = "OP_PRIVACY_ACCESS_LOG")
@Getter
@Setter
@NoArgsConstructor
public class PrivacyAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privacyAccessLogSeq")
    @SequenceGenerator(name = "privacyAccessLogSeq", sequenceName = "op_privacy_access_log_seq",
            allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** 접속일시 - DB는 timestamp다(AS-IS는 Instant). */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** 접속 IP - AS-IS는 pCrypto로 암호화해 넣는다(TO-BE는 암호화 모듈이 없어 평문). */
    @Column(name = "IP")
    private String ip;

    /** MANAGER / SELLER. */
    @Column(name = "LOGIN_TYPE")
    private String loginType;

    /** 처리자 ID - 관리자면 OP_MANAGER.USER_ID, 판매자면 OP_SELLER.SELLER_ID. */
    @Column(name = "MANAGER_ID")
    private Long managerId;

    @Column(name = "METHOD")
    private String method;

    /** 요청명 - AS-IS PrivacyAccess.name (화면의 "다운로드 메뉴"). */
    @Column(name = "NAME")
    private String name;

    /** EXCELDOWNLOAD_REASON_TYPE 코드. AS-IS 모달에 사유구분 select가 없어 실제로는 비어 있다. */
    @Column(name = "REASON_TYPE")
    private String reasonType;

    @Column(name = "REASON")
    private String reason;

    /** 수행업무 - AS-IS PrivacyTask.title. 1411 목록은 '엑셀 다운로드'인 행만 본다. */
    @Column(name = "TASK")
    private String task;

    @Column(name = "URL")
    private String url;

    /** 회원 ID - AS-IS는 현재 로그인 사용자의 userId를 넣는다. */
    @Column(name = "USER_ID")
    private Long userId;

    /** 화면 표시용 - managerId로 찾은 로그인ID(AS-IS 목록 쿼리의 LOGIN_ID 서브쿼리). */
    @Transient
    private String loginId;

    /** 화면 표시용 - EXCELDOWNLOAD_REASON_TYPE 라벨(AS-IS 목록 쿼리의 REASON_TYPE 서브쿼리). */
    @Transient
    private String reasonTypeName;

    /** AS-IS 목록의 등록일 표기. */
    @Transient
    public String getCreatedAtText() {
        return createdAt == null ? "" : createdAt.toString().replace('T', ' ');
    }
}
