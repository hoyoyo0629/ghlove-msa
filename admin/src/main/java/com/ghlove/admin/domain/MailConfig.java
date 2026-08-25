package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메일설정 관리 (AS-IS opmanager/mail-config) - 주문상태(ORDER_STATUS)별 발송 이메일
 *  템플릿. 구매자/관리자/판매자 3자 각각 제목+본문을 따로 관리한다. 배치 스캔 당시 이미
 *  시퀀스가 잡혀 있던 테이블이라 별도 보강 불필요. */
@Entity
@Table(name = "OP_MAIL_CONFIG")
@Getter
@Setter
@NoArgsConstructor
public class MailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mailConfigIdSeq")
    @SequenceGenerator(name = "mailConfigIdSeq", sequenceName = "op_mail_config_mail_config_id_seq", allocationSize = 1)
    @Column(name = "MAIL_CONFIG_ID")
    private Integer mailConfigId;

    @Column(name = "SMS_CONFIG")
    private String smsConfig;

    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "BUYER_SUBJECT")
    private String buyerSubject;

    @Column(name = "ADMIN_SUBJECT")
    private String adminSubject;

    @Column(name = "SELLER_SUBJECT")
    private String sellerSubject;

    @Column(name = "BUYER_CONTENT")
    private String buyerContent;

    @Column(name = "ADMIN_CONTENT")
    private String adminContent;

    @Column(name = "SELLER_CONTENT")
    private String sellerContent;
}
