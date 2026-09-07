package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** SFR-002 "인증토큰/세션 관리(재인증 정책)" - GH_AUTH 액세스 토큰 회전용 장기 불투명 토큰. */
@Entity
@Table(name = "USER_REFRESH_TOKEN")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userRefreshTokenIdSeq")
    @SequenceGenerator(name = "userRefreshTokenIdSeq",
            sequenceName = "user_refresh_token_token_id_seq", allocationSize = 1)
    @Column(name = "TOKEN_ID")
    private Long tokenId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "TOKEN")
    private String token;

    /** yyyyMMddHHmmss. */
    @Column(name = "EXPIRES_AT")
    private String expiresAt;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}
