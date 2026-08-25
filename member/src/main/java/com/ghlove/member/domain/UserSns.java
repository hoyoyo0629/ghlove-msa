package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 카카오/네이버 SNS 로그인 연동 (AS-IS OP_USER_SNS). */
@Entity
@Table(name = "OP_USER_SNS")
@Getter
@Setter
@NoArgsConstructor
public class UserSns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SNS_USER_ID")
    private Long snsUserId;

    /** SNS 공급자가 발급한 회원 고유 ID. */
    @Column(name = "SNS_ID")
    private String snsId;

    @Column(name = "USER_ID")
    private Long userId;

    /** kakao / naver */
    @Column(name = "SNS_TYPE")
    private String snsType;

    @Column(name = "SNS_NAME")
    private String snsName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "CERTIFIED_DATE")
    private LocalDateTime certifiedDate;
}
