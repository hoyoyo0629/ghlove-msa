package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 지자체별 연간 기부한도/포인트적립률 설정 (G_CTBNY_SETUP). Per-locgov and per-year,
 * so it must be looked up here rather than hardcoded (no-hardcoding principle).
 */
@Entity
@Table(name = "G_CTBNY_SETUP")
@IdClass(CtbnySetupId.class)
@Getter
@Setter
@NoArgsConstructor
public class CtbnySetup {

    @Id
    @Column(name = "STDR_YEAR")
    private String stdrYear;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "LMT_AMT")
    private Integer lmtAmt;
}
