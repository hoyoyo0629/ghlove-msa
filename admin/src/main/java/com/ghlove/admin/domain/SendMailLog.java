package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메일 발송 이력 (AS-IS opmanager/send-mail-log - SendMailLogManagerController).
 * Maps OP_SEND_MAIL_LOG. */
@Entity
@Table(name = "OP_SEND_MAIL_LOG")
@Getter
@Setter
@NoArgsConstructor
public class SendMailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEND_MAIL_LOG_ID")
    private Integer sendMailLogId;

    @Column(name = "SEND_NAME")
    private String sendName;

    @Column(name = "SEND_EMAIL")
    private String sendEmail;

    @Column(name = "RECEIVE_NAME")
    private String receiveName;

    @Column(name = "RECEIVE_EMAIL")
    private String receiveEmail;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    /** "Y"/"N" - 발송 성공 여부. */
    @Column(name = "SEND_FLAG")
    private String sendFlag;

    @Column(name = "SEND_DATE")
    private String sendDate;

    @Column(name = "MAIL_TYPE")
    private String mailType;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}
