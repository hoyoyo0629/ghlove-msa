package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 지자체별 기부포인트 적립률(연도별). point 서비스는 donation 서비스의 DB를 직접
 * 읽을 수 없으므로(DB per Service) 자체 사본을 보관한다 - 하드코딩 금지 원칙.
 */
@Entity
@Table(name = "PT_LOCGOV_POINT_RATE")
@IdClass(LocgovPointRateId.class)
@Getter
@Setter
@NoArgsConstructor
public class LocgovPointRate {

    @Id
    @Column(name = "STDR_YEAR")
    private String stdrYear;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "POINT_RATE")
    private BigDecimal pointRate;
}
