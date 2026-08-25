package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지자체담당자/제공자 역할 신청·승인 (SFR-002 "지자체 담당자/답례품 제공자의 승인·해지, 권한 부여·회수 절차 표준화"). */
@Entity
@Table(name = "OP_USER_ROLE_REQUEST")
@Getter
@Setter
@NoArgsConstructor
public class UserRoleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REQUEST_ID")
    private Long requestId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "REQUESTED_ROLE")
    private String requestedRole;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "PROCESSED_DATE")
    private LocalDateTime processedDate;
}
