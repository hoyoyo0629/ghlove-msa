package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 답례품 1:1 상담문의 구버전 (AS-IS opmanager/inquiry - InquiryManagerController).
 * AS-IS는 답변을 별도 텍스트로 저장하지 않고 SMS로 발송한 뒤 그 발송이력
 * (OP_SEND_SMS_LOG, SEND_TYPE='INQUIRY_{id}')로 "답변완료 여부"를 판단한다 - 이 프로젝트는
 * 실제 SMS 발송 연동이 없어 조회 전용으로만 구현한다. Maps OP_SHOP_INQUIRY. */
@Entity
@Table(name = "OP_SHOP_INQUIRY")
@Getter
@Setter
@NoArgsConstructor
public class ShopInquiry {

    @Id
    @Column(name = "INQUIRY_ID")
    private Integer inquiryId;

    @Column(name = "INQUIRY_TYPE")
    private String inquiryType;

    @Column(name = "ITEM_CODE")
    private String itemCode;

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "USER_EMAIL")
    private String userEmail;

    @Column(name = "TEL_NUMBER")
    private String telNumber;

    @Column(name = "INQUIRY_SUBJECT")
    private String inquirySubject;

    @Column(name = "INQUIRY_CONTENT")
    private String inquiryContent;

    /** 0=미답변, 1=답변완료. */
    @Column(name = "ANSWER_FLAG")
    private Integer answerFlag;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
