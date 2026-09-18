package com.ghlove.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** AS-IS `G_MBER_SECSN`의 복합 PK (탈퇴연도 + 회원ID + 본인확인 CI). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MberSecsnId implements Serializable {

    @Column(name = "SECSN_YEAR", length = 4)
    private String secsnYear;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "MBER_CI", length = 200)
    private String mberCi;
}
