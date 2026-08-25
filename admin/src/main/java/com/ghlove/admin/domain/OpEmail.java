package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 이메일 발송 (AS-IS opmanager/email) - 관리자가 대상(권한)을 골라 임의 메일을 작성/
 *  발송하는 도구. AS-IS는 구매자/판매자까지 아우르는 멀티벤더 대상 지정이지만, 이
 *  프로젝트는 admin 자신의 OP_MANAGER(관리자/운영자)만 대상으로 좁혔다 - 회원(donor)
 *  대상 발송은 member 서비스 소유 데이터라 cross-service 조회가 별도로 필요해서 범위 밖. */
@Entity
@Table(name = "OP_EMAIL")
@Getter
@Setter
@NoArgsConstructor
public class OpEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opEmailIdSeq")
    @SequenceGenerator(name = "opEmailIdSeq", sequenceName = "op_email_email_id_seq", allocationSize = 1)
    @Column(name = "EMAIL_ID")
    private Long emailId;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    /** yyyyMMddHHmmss. */
    @Column(name = "SEND_DATE")
    private String sendDate;

    /** R:작성중/등록, S:발송완료, F:발송실패. */
    @Column(name = "STATUS")
    private String status;

    @Column(name = "AUTH_TARGET")
    private String authTarget;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;
}
