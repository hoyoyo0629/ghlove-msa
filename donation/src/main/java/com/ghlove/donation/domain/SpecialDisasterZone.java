package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 특별재난지역 공고 (AS-IS OP_SPEL_DSTR_ZN). 기부확인증에서 해당 기간·지자체의
 * 기부 건에 "(특별재난지역)" 표기를 붙이는 데만 쓰인다.
 */
@Entity
@Table(name = "OP_SPEL_DSTR_ZN")
@Getter
@Setter
@NoArgsConstructor
public class SpecialDisasterZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long spelDstrZnId;

    @Column(name = "LOCGOV_NM")
    private String locgovNm;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "NOTI_REASON")
    private String notiReason;

    /** yyyyMMdd */
    @Column(name = "NOTI_DATE")
    private String notiDate;

    /** yyyyMMdd */
    @Column(name = "END_DATE")
    private String endDate;
}
