package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OP_USER_DETAIL")
@Getter
@Setter
@NoArgsConstructor
public class UserDetail {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "ADDRESS_DETAIL")
    private String addressDetail;

    @Column(name = "GENDER")
    private String gender;

    /** yyyyMMdd 형식 (도네이션 서비스의 기부확인증 등에서 사용). */
    @Column(name = "BIRTHDAY")
    private String birthday;

    @Column(name = "USE_FLAG")
    private String useFlag;

    @Column(name = "LEAVE_REASON")
    private String leaveReason;

    /** OP_COMMON_CODE(LEAVE_CODE) 라디오 선택값 (AS-IS users/secede.html leaveCodeList). */
    @Column(name = "LEAVE_CODE")
    private String leaveCode;

    /** 우편번호 (AS-IS users/modify.html "주소" - Daum 우편번호 API 없이 평문 입력). */
    @Column(name = "POST")
    private String post;

    @Column(name = "RECEIVE_EMAIL")
    private String receiveEmail;

    @Column(name = "RECEIVE_SMS")
    private String receiveSms;

    @Column(name = "RECEIVE_KAKAO")
    private String receiveKakao;

    /** OP_USER_LEVEL(saleson 멀티벤더 회원등급/할인율 체계) FK, NOT NULL 제약만 있고 이
     *  프로젝트는 등급 개념을 실제로 쓰지 않는다(마스터 데이터 자체가 비어있는 saleson
     *  boilerplate) - 가입 시 항상 1(기본 등급)로 채운다. */
    @Column(name = "LEVEL_ID")
    private Integer levelId;
}
