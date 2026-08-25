package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 회원별 잔액. 거래 원장(PointLedger)과 분리 관리 (SFR-004 명시 요구사항). */
@Entity
@Table(name = "PT_POINT_BALANCE")
@Getter
@Setter
@NoArgsConstructor
public class PointBalance {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "BALANCE")
    private Long balance;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
