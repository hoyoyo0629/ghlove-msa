package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 명예기부자 등급 (마이페이지 "기부혜택증"). 사용자가 해당 연도·지자체에 누적 기부한
 * 금액이 G_LOCGOV.STDR_1/2/3LEVEL_AMT 구간을 넘을 때마다 completeDonation()에서
 * upsert된다 - AS-IS도 별도 발급 버튼 없이 기부 완료 시점에 자동 산정한다.
 */
@Entity
@Table(name = "G_HONOR_CNTRBTR")
@IdClass(HonorCntrbtrId.class)
@Getter
@Setter
@NoArgsConstructor
public class HonorCntrbtr {

    @Id
    @Column(name = "STDR_YEAR")
    private Integer stdrYear;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "HONOR_CNTRBTR_LEVEL_CODE")
    private String honorCntrbtrLevelCode;
}
