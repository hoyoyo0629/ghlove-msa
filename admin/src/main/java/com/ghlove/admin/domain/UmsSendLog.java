package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** UMS 발송이력 (조회 전용). Maps OP_UMS_SEND_LOG. */
@Entity
@Table(name = "OP_UMS_SEND_LOG")
@Getter
@Setter
@NoArgsConstructor
public class UmsSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TEMPLATE_CODE")
    private String templateCode;

    @Column(name = "UMS_TYPE")
    private String umsType;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "TARGET_USER_ID")
    private Long targetUserId;

    @Column(name = "CREATED")
    private LocalDateTime created;

    /** SFR-007 재검토 라운드 - 실제 발송경로(NotificationClient)와 이 이력 화면을 연결하며
     *  추가한 필드. 발송 대상이 항상 순수 회원 ID는 아니라서(예: "seller:123") 원본 키를
     *  그대로 남긴다. */
    @Column(name = "TARGET_KEY")
    private String targetKey;

    @Column(name = "SUCCESS_YN")
    private String successYn;

    @Column(name = "RETRY_COUNT")
    private Integer retryCount;
}
