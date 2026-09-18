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

    /** 국민비서 알림서비스 수신동의 (AS-IS users/modify.html의 receivePbanc - v-model로
     *  실제 저장되는 값이다. MSA는 이 항목만 화면에서 disabled로 막아둬 재현이 빠져 있었다).
     *  AS-IS는 0=수신/1=비수신을 쓰지만 여기서는 나머지 3개 수신동의와 같은 'Y'/'N'으로
     *  통일한다 - RECEIVE_EMAIL/SMS/KAKAO가 전부 Y/N이라 한 화면에서 규약이 갈리면 안 된다. */
    @Column(name = "RECEIVE_PBANC")
    private String receivePbanc;

    /** OP_USER_LEVEL(saleson 멀티벤더 회원등급/할인율 체계) FK, NOT NULL 제약만 있고 이
     *  프로젝트는 등급 개념을 실제로 쓰지 않는다(마스터 데이터 자체가 비어있는 saleson
     *  boilerplate) - 가입 시 항상 1(기본 등급)로 채운다. */
    @Column(name = "LEVEL_ID")
    private Integer levelId;
}
