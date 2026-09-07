package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** SMS 발송 이력 (AS-IS opmanager/send-sms-log - SendSmsLogManagerController).
 * Maps OP_SEND_SMS_LOG. */
@Entity
@Table(name = "OP_SEND_SMS_LOG")
@Getter
@Setter
@NoArgsConstructor
public class SendSmsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEND_SMS_LOG_ID")
    private Integer sendSmsLogId;

    @Column(name = "SEND_TEL_NUMBER")
    private String sendTelNumber;

    @Column(name = "RECEIVE_TEL_NUMBER")
    private String receiveTelNumber;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "SEND_TYPE")
    private String sendType;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}
