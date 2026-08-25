package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지자체별 기부혜택 안내문구 (SFR-003 "지자체별 기부혜택 관리"). 세액공제 계산은 전국
 * 공통 법정 비율이라 서비스 로직으로 하고, 이 테이블은 지자체 자체 추가 혜택만 관리한다. */
@Entity
@Table(name = "G_HONOR_BENEFIT")
@Getter
@Setter
@NoArgsConstructor
public class HonorBenefit {

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "BENEFIT_DESC")
    private String benefitDesc;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
