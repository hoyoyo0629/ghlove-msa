package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메일설정 관리 (AS-IS opmanager/mail-config) - 메일 템플릿별 제목/본문/발송여부.
 *  템플릿 종류는 AS-IS {@code MailTemplate.getTemplateCodes()}의 고정 10종이다
 *  (입금대기·결제완료·배송중·포인트소멸예정·회원가입·임시비밀번호 안내·문의답변·휴면안내·
 *  관리자 권한 승인/거절) - 주문상태 코드가 아니다. 구매자/관리자/판매자 3자 각각 제목+본문을
 *  따로 관리하고, 모바일용 제목/본문과 HTML 태그 사용여부도 AS-IS에 있다. */
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

    /** 발송여부 Y/N - 이메일 설정 화면의 "발송여부" 라디오가 이 값이다. */
    @Column(name = "BUYER_SEND_FLAG")
    private String buyerSendFlag;

    /** AS-IS 폼은 hidden으로 'N'을 고정해 넘긴다(관리자 수신 항목이 주석처리된 상태). */
    @Column(name = "ADMIN_SEND_FLAG")
    private String adminSendFlag;

    @Column(name = "SELLER_SEND_FLAG")
    private String sellerSendFlag;

    @Column(name = "BUYER_TAG_USE")
    private String buyerTagUse;

    @Column(name = "ADMIN_TAG_USE")
    private String adminTagUse;

    @Column(name = "SELLER_TAG_USE")
    private String sellerTagUse;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "MOBILE_BUYER_SUBJECT")
    private String mobileBuyerSubject;

    @Column(name = "MOBILE_ADMIN_SUBJECT")
    private String mobileAdminSubject;

    @Column(name = "MOBILE_SELLER_SUBJECT")
    private String mobileSellerSubject;

    @Column(name = "MOBILE_BUYER_CONTENT")
    private String mobileBuyerContent;

    @Column(name = "MOBILE_ADMIN_CONTENT")
    private String mobileAdminContent;

    @Column(name = "MOBILE_SELLER_CONTENT")
    private String mobileSellerContent;

    /** AS-IS 목록의 "회원 메일수신" 컬럼 - 발송(Y)/미발송(N) 텍스트. */
    @Transient
    public String getBuyerSendFlagText() {
        return "Y".equals(buyerSendFlag) ? "발송" : "미발송";
    }

    /** AS-IS 목록의 "관리자 메일수신" 컬럼. */
    @Transient
    public String getAdminSendFlagText() {
        return "Y".equals(adminSendFlag) ? "발송" : "미발송";
    }
}
