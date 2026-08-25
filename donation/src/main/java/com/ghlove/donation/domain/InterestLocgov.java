package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관심지자체 (마이페이지 "관심지자체"). Maps a subset of the AS-IS G_INTRST_LOCGOV columns. */
@Entity
@Table(name = "G_INTRST_LOCGOV")
@IdClass(InterestLocgovId.class)
@Getter
@Setter
@NoArgsConstructor
public class InterestLocgov {

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    /** yyyyMMdd. */
    @Column(name = "REGIST_DE")
    private String registDe;
}
