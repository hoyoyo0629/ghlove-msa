package com.ghlove.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 탈퇴 시 본인인증 CI 백업(AS-IS insertSecedeCustomer → op_user_ci). OP_USER.MBER_CI를
 * NULL로 지우기 전에 이 표에 CI를 옮겨 담아, 탈퇴 이후에도 CI 기반 조회(중복 탈퇴 방지 등)가
 * 가능하도록 남긴다. PK는 user_id 단일.
 */
@Entity
@Table(name = "OP_USER_CI")
@Getter
@Setter
@NoArgsConstructor
public class UserCi {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "MBER_CI")
    private String mberCi;

    public UserCi(Long userId, String mberCi) {
        this.userId = userId;
        this.mberCi = mberCi;
    }
}
